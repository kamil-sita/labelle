package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.datasource.util.CloseableIterator;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
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
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();

		try (CloseableIterator<ImageResponse> images = (inRepositoryService.images().images()).process().filterByRepository(parameter.repositoryId()).getIterator()) {
			images.forEachRemaining(ir -> {
				image.add(ir.id());
				persistableImagesTags.addTag(ir.id(), new Tag(parameter.category, parameter.tag));
			});
		}
		inRepositoryService.addTags(persistableImagesTags);

		return TaskResult.success(new Response(parameter.repositoryId, image, parameter.category, parameter.tag));
	}

	@Override
	public String sampleValue() {
		return serializeParam(new Config(UUID.randomUUID(), "category", "tag"));
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

	public record Config(UUID repositoryId, String category, String tag) {

	}

	public record Response(UUID repositoryId, Set<UUID> images, String category, String tag) {

	}
}
