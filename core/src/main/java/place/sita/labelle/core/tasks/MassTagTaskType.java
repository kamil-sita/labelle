package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.datasource.util.CloseableIterator;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

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
		InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags(parameter.repositoryId);

		try (CloseableIterator<ImageResponse> images = inRepositoryService.images().process().filterByRepository(parameter.repositoryId()).getIterator()) {
			images.forEachRemaining(ir -> {
				image.add(ir.id());
				persistableImagesTags.addTag(ir.id(), parameter.tag, parameter.family);
			});
		}
		inRepositoryService.addTags(persistableImagesTags);

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
