package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.Root;
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
		findAllFilesInDir(new File(directory), fileList);

		Set<UUID> images = new HashSet<>();
		Map<UUID, Set<String>> tagsToImages = new HashMap<>();

		for (File file : fileList) {
			var result = taskContext.getApi().getInRepositoryService()
				.addImage(parameter.repositoryId, file);

			if (result.isSuccess()) {
				var img = result.getSuccess();
				images.add(img.id());
				tagsToImages.put(img.id(), new HashSet<>());
				List<String> tags = getDirectoriesInPath(file.getPath(), directory);
				for (String tag : tags) {
					tagsToImages.get(img.id()).add(tag);
					taskContext.getApi().getInRepositoryService().addTag(img.id(), parameter.repositoryId, new Tag("folder", tag));
				}
			} else {
				taskContext.log("Failed to add image: " + file.getAbsolutePath());
			}

		}

		return TaskResult.success(new Response(parameter.repositoryId, images, tagsToImages));
	}

	private static void findAllFilesInDir(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					fileList.add(file);
				} else if (file.isDirectory()) {
					findAllFilesInDir(file, fileList);
				}
			}
		}
	}

	private static List<String> getDirectoriesInPath(String filePath, String basePath) {
		List<String> directories = new ArrayList<>();
		File file = new File(filePath);
		File base = new File(basePath);

		// Ensure the file path contains the base path
		if (file.getAbsolutePath().startsWith(base.getAbsolutePath())) {
			File parent = file.getParentFile();

			// Climb up the directory tree until reaching the base
			while (parent != null && !parent.equals(base)) {
				directories.add(0, parent.getName()); // Add at the beginning of the list
				parent = parent.getParentFile();
			}
		}

		return directories;
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
