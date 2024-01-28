package place.sita.labelle.core.tasks.buitlin;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.core.tasks.TaskContext;
import place.sita.labelle.core.tasks.TaskResult;
import place.sita.labelle.core.tasks.TaskType;
import place.sita.labelle.core.tasks.scheduler.resources.resource.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class MassTagTaskType implements TaskType<MassTagTaskType.Config, RepositoryApi, MassTagTaskType.Response> {

	@Override
	public String code() {
		return "mass-tag";
	}

	@Override
	public String name() {
		return "Mass tag";
	}

	@Override
	public TaskResult<Response> runTask(Config parameter, TaskContext<RepositoryApi> taskContext) {

		Set<UUID> image = new HashSet<>();
		taskContext.getApi().getInRepositoryService().images(parameter.repositoryId, 0, Integer.MAX_VALUE, "")
			.forEach(ir -> {
				image.add(ir.id());
				taskContext.getApi().getInRepositoryService()
					.addTag(ir.id(), parameter.repositoryId, parameter.tag, parameter.family);
			});

		return TaskResult.success(new Response(parameter.repositoryId, image, parameter.family, parameter.tag));
	}

	@Override
	public String sampleValue() {
		return serializeParam(new Config(UUID.randomUUID(), "family", "tag"));
	}

	@Override
	public List<Resource<?>> resources(Config config) {
		return List.of();
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

	public record Config(UUID repositoryId, String family, String tag) {

	}

	public record Response(UUID repositoryId, Set<UUID> images, String family, String tag) {

	}
}
