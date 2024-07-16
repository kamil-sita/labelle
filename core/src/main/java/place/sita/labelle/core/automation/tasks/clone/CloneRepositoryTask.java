package place.sita.labelle.core.automation.tasks.clone;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.automation.tasks.AutomationTasksCommon;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.datasource.util.CloseableIterator;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

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

        addParents(parameter, taskContext, newRepoId);

        copyImages(parameter, taskContext, newRepoId);

        return TaskResult.success(newRepoId);
    }

    private static void copyImages(CloneRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext, UUID newRepoId) {
        InRepositoryService inRepositoryService = taskContext.getApi().getInRepositoryService();

        try (CloseableIterator<ImageResponse> imageIterator = inRepositoryService.images().process().filterByRepository(parameter.repositoryToClone()).getIterator()) {
            imageIterator.forEachRemaining(image -> {
                UUID imageId = copyImage(taskContext, newRepoId, image);

                addTags(taskContext, newRepoId, image, imageId);
            });
        }
    }

    private static void addTags(TaskContext<RepositoryApi> taskContext, UUID newRepoId, ImageResponse image, UUID imageId) {
        PersistableImagesTags persistableImagesTags = new PersistableImagesTags(newRepoId);

        taskContext.getApi()
                .getInRepositoryService()
                .getTags(image.id())
                .forEach(tagResponse -> {
                    persistableImagesTags.addTag(imageId, tagResponse.category(), tagResponse.tag());
                });

        taskContext.getApi()
            .getInRepositoryService()
            .addTags(persistableImagesTags);
    }

    private static UUID copyImage(TaskContext<RepositoryApi> taskContext, UUID newRepoId, ImageResponse image) {
        UUID imageId = taskContext.getApi()
                .getInRepositoryService()
                .copyImage(newRepoId, image.id());
        return imageId;
    }

    private static void addParents(CloneRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext, UUID newRepoId) {
        taskContext
                .getApi()
                .getRepositoryService()
                .getParents(parameter.repositoryToClone())
                .forEach(parent -> {
                    UUID remappedParent = parameter.parentsRemapping().getOrDefault(parent.id(), parent.id());
                    taskContext.getApi().getRepositoryService().addParentChild(newRepoId, remappedParent);
                });
    }

    private static void createRepo(CloneRepositoryTaskInput parameter, TaskContext<RepositoryApi> taskContext, UUID newRepoId) {
        taskContext
                .getApi()
                .getRepositoryService()
                .addRepository(newRepoId, parameter.newRepositoryName());
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
