package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.io.File;
import java.util.*;

@Component
public class ImportImagesTreatSubdirectoriesAsTags implements TaskType<ImportImagesTreatSubdirectoriesAsTags.Config, RepositoryApi, ImportImagesTreatSubdirectoriesAsTags.Response> {


	@Override
	public String code() {
		return "import-images-treat-subdirectories-as-tags";
	}

	@Override
	public String name() {
		return "Import images, treat subdirectories as tags";
	}

	@Override // todo this should probably be a transactional
	public TaskResult<Response> runTask(Config parameter, TaskContext<RepositoryApi> taskContext) {
		Root root = taskContext.getApi().getInRepositoryService().roots()
			.stream().filter(r -> r.id().equals(parameter.rootId)).findFirst().get();

		String directory = root.directory();

		List<File> fileList = new ArrayList<>();
		Utils.findAllFilesInDir(new File(directory), fileList);

		Set<UUID> images = new HashSet<>();
		Map<UUID, Set<String>> tagsToImages = new HashMap<>();

		PersistableImagesTags pit = new PersistableImagesTags();

		for (File file : fileList) {
			InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();
			var result = inRepositoryService.images().addImage(parameter.repositoryId, file);

			if (result.isSuccess()) {
				var img = result.getSuccess();
				images.add(img.id());
				tagsToImages.put(img.id(), new HashSet<>());
				List<String> tags = getTagsInPath(file.getPath(), directory);

				for (String tag : tags) {
					tagsToImages.get(img.id()).add(tag);
					pit.addTag(img.id(), new Tag("folder", tag));
				}
			} else {
				taskContext.log("Failed to add image: " + file.getAbsolutePath());
			}

		}

		taskContext.getApi().getInRepositoryService().addTags(pit);

		return TaskResult.success(new Response(parameter.repositoryId, images, tagsToImages));
	}

	private static List<String> getTagsInPath(String filePath, String basePath) {
		List<String> tags = new ArrayList<>();
		File file = new File(filePath);
		File base = new File(basePath);

		// Ensure the file path contains the base path
		if (file.getAbsolutePath().startsWith(base.getAbsolutePath())) {
			File parent = file.getParentFile();

			// Climb up the directory tree until reaching the base
			while (parent != null && !parent.equals(base)) {
				String unprocessedTags = parent.getName();
				Arrays.stream(unprocessedTags.split(","))
						.forEach(tag -> tags.add(tag.trim()));
				parent = parent.getParentFile();
			}
		}

		return tags;
	}

	@Override
	public String sampleValue() {
		return serializeParam(new Config(UUID.randomUUID(), UUID.randomUUID()));
	}

	@Override
	public List<Resource<?>> resources(Config config) {
		return List.of(); // todo drives
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<Config> paramType() {
		return Config.class;
	}

	@Override
	public Class<Response> resultType() {
		return Response.class;
	}

	public record Config(UUID repositoryId, UUID rootId) {

	}

	public record Response(UUID repositoryId, Set<UUID> importedImages, Map<UUID, Set<String>> tags) {

	}
}
