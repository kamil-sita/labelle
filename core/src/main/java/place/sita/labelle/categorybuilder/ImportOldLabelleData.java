package place.sita.labelle.categorybuilder;

import org.springframework.stereotype.Component;
import place.sita.labelle.categorybuilder.model.Categories;
import place.sita.labelle.categorybuilder.model.ImagesCategories;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.MappingSupport;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ImportOldLabelleData implements TaskType<ImportOldLabelleDataInput, RepositoryApi, Void> {

	@Override
	public TaskResult<Void> runTask(ImportOldLabelleDataInput parameter, TaskContext<RepositoryApi> taskContext) {
		try {
			return runTaskActual(parameter, taskContext);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static TaskResult<Void> runTaskActual(ImportOldLabelleDataInput parameter, TaskContext<RepositoryApi> taskContext) throws IOException {
		Path categoryPath = Paths.get(parameter.categoriesFile());
		Path categoryImagesPath = Paths.get(parameter.imageCategoriesFile());
		Categories categories = MappingSupport.objectMapper.readValue(categoryPath.toFile(), Categories.class);
		ImagesCategories imagesCategories = MappingSupport.objectMapper.readValue(categoryImagesPath.toFile(), ImagesCategories.class);

		Map<UUID, String> categoryUuidCache = new HashMap<>();
		Map<UUID, String> tagUuidCache = new HashMap<>();

		InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();
		categories.categories().forEach(category -> {
			categoryUuidCache.put(category.categoryUuid(), category.name());
			category.categoryValues().forEach(categoryValue -> {
				tagUuidCache.put(categoryValue.categoryValueUuid(), categoryValue.taughtValue());
			});
		});

		imagesCategories.imageCategories().forEach(imageCategory -> {
			String path = parameter.root() + imageCategory.path();
			var result = inRepositoryService.addImage(parameter.repositoryId(), Paths.get(path).toFile());
			if (result.isSuccess()) {
				UUID imageId = result.getSuccess().getId();
				PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
				imageCategory.imageCategoriesValues().forEach(imageCategoriesValue -> {
					String category = categoryUuidCache.get(imageCategoriesValue.categoryUuid());
					String baseTag = tagUuidCache.get(imageCategoriesValue.categoryValueUuid());
					String modifier = imageCategoriesValue.descriptiveModifier();

					if (category == null || baseTag == null || modifier == null) {
						taskContext.log("Corrupted data for image: " + path);
						return;
					}

					String actualTag = modifier.replace("[]", baseTag);
					if (!actualTag.isEmpty()) {
						persistableImagesTags.addTag(imageId, new Tag(category, actualTag));
					}
				});
				inRepositoryService.addTags(persistableImagesTags);
			} else {
				taskContext.log("Failed to add image: " + path);
			}
		});

		//imageCategories

		return TaskResult.success(null);
	}

	@Override
	public String code() {
		return "import-old-labelle-data-v1";
	}

	@Override
	public String name() {
		return "Import old labelle data";
	}

	@Override
	public String sampleValue() {
		return serializeParam(new ImportOldLabelleDataInput(
			"C:/path1",
			"C:/path2",
			UUID.randomUUID(),
			"C:/root"
		));
	}

	@Override
	public List<Resource<?>> resources(ImportOldLabelleDataInput importOldLabelleDataInput) {
		return List.of(); // todo HDD, repository
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<ImportOldLabelleDataInput> paramType() {
		return ImportOldLabelleDataInput.class;
	}

	@Override
	public Class<Void> resultType() {
		return Void.class;
	}
}
