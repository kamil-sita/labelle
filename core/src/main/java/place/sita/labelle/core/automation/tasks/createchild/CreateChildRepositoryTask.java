package place.sita.labelle.core.automation.tasks.createchild;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.automation.tasks.AutomationTasksCommon;
import place.sita.labelle.core.repository.acrossrepository.PullableImageResponse;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.*;

@Component
public class CreateChildRepositoryTask implements TaskType<CreateChildRepositoryTaskInput, RepositoryApi, UUID> {
	@Override
	public String code() {
		return "create-child-repo-v1";
	}

	@Override
	public String name() {
		return "Create child repository";
	}

	@Override
	@Transactional
	public TaskResult<UUID> runTask(CreateChildRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext) {
		UUID newRepoId = parameter.newRepositoryId() == null ? UUID.randomUUID() : parameter.newRepositoryId();

		AutomationTasksCommon.createRepo(parameter.newRepositoryName(), taskContext, newRepoId);

		addParents(parameter, taskContext, newRepoId);

		Map<String, List<UUID>> parentImagesReferences = getParentImagesReferences(parameter, taskContext);

		Map<String, UUID> referenceToNewImageId = createImagesInNewRepo(taskContext, parentImagesReferences, newRepoId);

		addTagsToNewImages(taskContext, parentImagesReferences, referenceToNewImageId);

		return TaskResult.success(newRepoId);
	}

	private static Map<UUID, List<Tag>> getTagsOfParents(TaskContext<RepositoryApi> taskContext, Map<String, List<UUID>> parentImagesReferences) {
		List<UUID> allReferencedImages = parentImagesReferences.values()
			.stream()
			.flatMap(Collection::stream)
			.toList();

		return taskContext.getApi().getAcrossRepositoryService().getTags(allReferencedImages);
	}

	private static void addTagsToNewImages(TaskContext<RepositoryApi> taskContext, Map<String, List<UUID>> parentImagesReferences, Map<String, UUID> referenceToNewImageId) {
		Map<UUID, List<Tag>> tags = getTagsOfParents(taskContext, parentImagesReferences);

		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();

		for (var newImage : referenceToNewImageId.entrySet()) {
			String newImageReference = newImage.getKey();
			UUID newImageId = newImage.getValue();
			List<UUID> parentImageIds = parentImagesReferences.get(newImageReference);
			for (UUID parentImageId : parentImageIds) {
				List<Tag> tagValues = tags.get(parentImageId);
				for (Tag tagValue : tagValues) {
					persistableImagesTags.addTag(newImageId, tagValue);
				}
			}
		}

		taskContext.getApi().getInRepositoryService().addTags(persistableImagesTags);
	}

	private static Map<String, UUID> createImagesInNewRepo(TaskContext<RepositoryApi> taskContext, Map<String, List<UUID>> parentImagesReferences, UUID newRepoId) {
		Map<String, UUID> referenceToNewImageId = new HashMap<>();

		parentImagesReferences.forEach((reference, parents) -> {
			UUID imageId = taskContext.getApi().getInRepositoryService().referImage(newRepoId, parents.get(0), reference);
			referenceToNewImageId.put(reference, imageId);
		});
		return referenceToNewImageId;
	}

	private static Map<String, List<UUID>> getParentImagesReferences(CreateChildRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext) {
		Map<String, List<UUID>> referencesToImageIds = new HashMap<>(); // I think it's safe to say that pulling from 2+ images
		// with vastly different configuration should be undefined. // todo report that

		for (UUID parent : parameter.parents()) {
			List<PullableImageResponse> imagesInRepo = taskContext.getApi().getAcrossRepositoryService().getImages(parent, 0, Integer.MAX_VALUE, "");

			imagesInRepo.forEach(image -> {
				if (referencesToImageIds.containsKey(image.reference())) {
					referencesToImageIds.get(image.reference()).add(image.imageId());
				} else {
					List<UUID> imageIds = new ArrayList<>();
					imageIds.add(image.imageId());
					referencesToImageIds.put(image.reference(), imageIds);
				}
			});
		}
		return referencesToImageIds;
	}

	private static void addParents(CreateChildRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext, UUID newRepoId) {
		parameter.parents().forEach(parent -> {
			taskContext.getApi().getRepositoryService().addParentChild(newRepoId, parent);
		});
	}

	@Override
	public String sampleValue() {
		return serializeParam(new CreateChildRepositoryTaskInput(
			List.of(UUID.randomUUID()),
			UUID.randomUUID(),
			"new-repo-name"
		));
	}

	@Override
	public List<Resource<?>> resources(CreateChildRepositoryTaskInput createChildRepositoryTaskInput) {
		return List.of(); // todo repo should be a resource tbh
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<CreateChildRepositoryTaskInput> paramType() {
		return CreateChildRepositoryTaskInput.class;
	}

	@Override
	public Class<UUID> resultType() {
		return UUID.class;
	}
}
