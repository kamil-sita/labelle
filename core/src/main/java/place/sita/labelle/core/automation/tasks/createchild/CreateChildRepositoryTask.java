package place.sita.labelle.core.automation.tasks.createchild;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.automation.tasks.AutomationTasksCommon;
import place.sita.labelle.core.repository.inrepository.image.replication.ReplicationParam;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.util.List;
import java.util.UUID;

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

		taskContext.getApi().getImageReplication().execute(new ReplicationParam.FillChildRepo(
			parameter.parents(),
			newRepoId
		));

		return TaskResult.success(newRepoId);
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
