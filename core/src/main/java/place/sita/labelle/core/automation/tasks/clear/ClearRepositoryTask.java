package place.sita.labelle.core.automation.tasks.clear;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;
import java.util.UUID;

@Component
public class ClearRepositoryTask implements TaskType<ClearRepositoryTaskInput, RepositoryApi, UUID> {
	@Override
	public String code() {
		return "clear-repository-v1";
	}

	@Override
	public String name() {
		return "Clear repository";
	}

	@Override
	@Transactional
	public TaskResult<UUID> runTask(ClearRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext) {
		taskContext
			.getApi()
			.getInRepositoryService()
			.images(parameter.repositoryId(), 0, Integer.MAX_VALUE, "")
			.forEach(image -> {
				taskContext
					.getApi()
					.getInRepositoryService()
					.deleteImage(image.id());
			});

		return TaskResult.success(parameter.repositoryId());
	}

	@Override
	public String sampleValue() {
		return serializeParam(new ClearRepositoryTaskInput(UUID.randomUUID()));
	}

	@Override
	public List<Resource<?>> resources(ClearRepositoryTaskInput clearRepositoryTaskInput) {
		return List.of(); // todo repo should be a resource tbh
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<ClearRepositoryTaskInput> paramType() {
		return ClearRepositoryTaskInput.class;
	}

	@Override
	public Class<UUID> resultType() {
		return UUID.class;
	}
}
