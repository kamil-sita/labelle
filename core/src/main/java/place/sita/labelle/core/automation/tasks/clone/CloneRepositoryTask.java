package place.sita.labelle.core.automation.tasks.clone;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.automation.tasks.AutomationTasksCommon;
import place.sita.labelle.core.repository.inrepository.image.replication.ReplicationParam;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Component
public class CloneRepositoryTask implements TaskType<CloneRepositoryTaskInput, RepositoryApi, UUID> {

    @Override
    public String code() {
        return "clone-repo-v1";
    }

    @Override
    public String name() {
        return "Clone repo";
    }

    @Override
    @Transactional
    public TaskResult<UUID> runTask(CloneRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext) {
        UUID newRepoId = parameter.newRepositoryId() == null ? UUID.randomUUID() : parameter.newRepositoryId();

        AutomationTasksCommon.createRepo(parameter.newRepositoryName(), taskContext, newRepoId);

        taskContext.getApi().getImageReplication().execute(new ReplicationParam.HardCopyToNewRepo(
            parameter.repositoryToClone(),
            newRepoId
        ));

        return TaskResult.success(newRepoId);
    }

    @Override
    public String sampleValue() {
        return serializeParam(new CloneRepositoryTaskInput(UUID.randomUUID(), UUID.randomUUID(), "new-repo", new HashMap<>()));
    }

    @Override
    public List<Resource<?>> resources(CloneRepositoryTaskInput cloneRepositoryTaskInput) {
        return List.of(); // todo repo should be a resource tbh
    }

    @Override
    public Class<RepositoryApi> contextType() {
        return RepositoryApi.class;
    }

    @Override
    public Class<CloneRepositoryTaskInput> paramType() {
        return CloneRepositoryTaskInput.class;
    }

    @Override
    public Class<UUID> resultType() {
        return UUID.class;
    }
}
