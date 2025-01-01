package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

@Component
public class ImportImagesUseTextFilesForTags implements TaskType<ImportImagesUseTextFilesForTags.Config, RepositoryApi, ImportImagesUseTextFilesForTags.Response> {

	@Override
	public TaskResult<Response> runTask(Config parameter, TaskContext<RepositoryApi> taskContext) {
		Root root = taskContext.getApi().getInRepositoryService().roots()
			.stream().filter(r -> r.id().equals(parameter.rootId)).findFirst().get();

		String directory = root.directory();
		List<File> fileList = new ArrayList<>();
		Utils.findAllFilesInDir(new File(directory), fileList);

		// if the file doesn't have an extension, then it's uninteresting to us (although a bit suspicious)
		// if it has multiple, let's just take the last one.
		// group "Files" by common path ignoring extension
		Map<String, Set<File>> filesByPath = new HashMap<>();
		for (File file : fileList) {
			String path = file.getPath();
			String extension = getFileExtension(file);
			if (extension.isEmpty()) {
				continue;
			}
			path = path.substring(0, path.length() - extension.length());
			filesByPath.computeIfAbsent(path, k -> new HashSet<>()).add(file);
		}

		List<String> unrecognizedFiles = new ArrayList<>();
		// now, in every group of files, we should have exactly one binary file, and multiple text files
		PersistableImagesTags pit = new PersistableImagesTags();
		int images = 0;
		for (Map.Entry<String, Set<File>> entry : filesByPath.entrySet()) {
			Map<String, List<String>> categoryLines = new HashMap<>();
			boolean hasBinary = false;
			File binaryFile = null;
			boolean hasMultipleBinaries = false;

			for (File file : entry.getValue()) {
				try {
					CharBuffer cb = Charset.availableCharsets().get("UTF-8").newDecoder()
						.decode(ByteBuffer.wrap(Files.readAllBytes(file.toPath())));
					// was valid
					String ext = getFileExtension(file);
					if (parameter.bannedTxtExtensions().contains(ext)) {
						continue;
					}
					List<String> lines = Arrays.asList(cb.toString().split("\n"));
					List<String> linesParsed = new ArrayList<>();
					categoryLines.put(getFileExtension(file), linesParsed);
					for (String line : lines) {
						line = line.replace("\r", "");
						if (line.isBlank()) {
							continue;
						}
						linesParsed.add(line);

					}
				} catch (CharacterCodingException e) {
					if (hasBinary) {
						hasMultipleBinaries = true;
						taskContext.log("Multiple binary files in " + entry.getKey() + ". Newest one is " + file.getPath());
					} else {
						hasBinary = true;
						binaryFile = file;
					}
				} catch (IOException e) {
					taskContext.log("Failed to read file: " + file.getPath());
				}
			}

			if (hasMultipleBinaries) {
				continue; // already logged
			}

			if (hasBinary) {
				InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();
				var result = inRepositoryService.images().addImage(parameter.repositoryId, binaryFile);
				if (result.isSuccess()) {
					var img = result.getSuccess();
					images++;
					categoryLines.forEach((category, lines) -> {
						for (String line : lines) {
							pit.addTag(img.id(), new Tag(category, line));
						}
					});
				} else {
					taskContext.log("Failed to add image: " + entry.getKey());
				}
			} else {
				taskContext.log("No binary file in " + entry.getKey());
				unrecognizedFiles.add(entry.getKey());
			}
		}

		taskContext.getApi().getInRepositoryService().addTags(pit);

		return TaskResult.success(new Response(parameter.repositoryId, images, unrecognizedFiles));
	}

	private static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf + 1);
	}

	@Override
	public String sampleValue() {
		return serializeParam(new Config(UUID.randomUUID(), UUID.randomUUID(), Set.of("illegalextension")));
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

	@Override
	public String code() {
		return "import-images-use-text-files-for-tags-v1";
	}

	@Override
	public String name() {
		return "Import images, use text files for tags";
	}

	public record Config(UUID repositoryId, UUID rootId, Set<String> bannedTxtExtensions) {

	}

	public record Response(UUID repositoryId, long imageCount, List<String> unrecognizedFiles) {

	}

}
