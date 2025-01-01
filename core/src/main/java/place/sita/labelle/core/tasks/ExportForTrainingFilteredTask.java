package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

@Component
public class ExportForTrainingFilteredTask implements TaskType<ExportForTrainingFilteredTask.Config, RepositoryApi, Void> {

	@Override
	public String code() {
		return "export-filtered-v1";
	}

	@Override
	public String name() {
		return "Export v1 with filtering";
	}

	@Override
	public TaskResult<Void> runTask(Config parameter, TaskContext<RepositoryApi> taskContext) {

		InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();
		(inRepositoryService.images().images())
			.process()
			.filterByRepository(parameter.repositoryId)
			.process()
			.filterUsingTfLang(parameter.query)
			.forEach(imageResponse -> {
				var ptr = imageResponse.toPtr();
				if (ptr instanceof ImagePtr.ImageOnPath imageOnPath) { // todo no hacks
					String wipFile = imageOnPath.root() + "/" + imageOnPath.path();
					File file = new File(wipFile);
					String onlyFileName = file.getName();
					File dest = new File(parameter.directory + "/" + onlyFileName);
					try {
						Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					List<String> tags = taskContext.getApi().getInRepositoryService()
						.getTags(imageResponse.id())
						.stream()
						.map(Tag::tag)
						.toList();
					String tag = String.join(", ", tags);
					File tagText = new File(parameter.directory + "/" + onlyFileName.split("\\.")[0] + ".txt");
					try {
						Files.writeString(tagText.toPath(), tag, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});

		return TaskResult.success();
	}

	@Override
	public String sampleValue() {
		return serializeParam(new Config(UUID.randomUUID(), "C:/mydir", "IN tags EXISTS (tag = \"dog\")"));
	}

	@Override
	public List<Resource<?>> resources(Config config) {
		return List.of(); // todo drive
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
	public Class<Void> resultType() {
		return Void.class;
	}

	public record Config(UUID repositoryId, String directory, String query) {

	}

}
