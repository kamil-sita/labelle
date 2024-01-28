package place.sita.labelle.core.tasks;

import org.apache.logging.log4j.util.Strings;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.core.utils.Result2;
import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.enums.TaskStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.*;

@Component
public class ExecutionsService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsService.class);

    private final DSLContext dslContext;
    private final TaskTypeRepository taskTypeRepository;

    public ExecutionsService(DSLContext dslContext, TaskTypeRepository taskTypeRepository) {
        this.dslContext = dslContext;
        this.taskTypeRepository = taskTypeRepository;
    }

    public int getJobsCountByTaskId(UUID id) {
        return dslContext
            .fetchCount(TASK, getIdCondition(id));
    }

    public List<ScheduledTaskResponse> getScheduledTasks(int limit, int offset, UUID id) {
        return dslContext
            .select(TASK.TASK_TYPE_ID, TASK.STATUS, TASK.CREATION_DATE, TASK.ID)
            .from(TASK)
            .where(getIdCondition(id))
            .orderBy(TASK.CREATION_DATE.desc())
            .offset(offset)
            .limit(limit)
            .fetch(rr -> {
                TaskType taskType = taskTypeRepository.byUUID(rr.value1());
                if (taskType == null) {
                    String code = dslContext
                        .select(TASK_TYPE.CODE)
                        .from(TASK_TYPE)
                        .where(TASK_TYPE.ID.eq(rr.value1()))
                        .fetchOne(0, String.class);

                    log.warn("Task type with id {} not found in memory. Code is \"{}\". This configuration is not supported", rr.value1(), code);
                    return null;
                }
                return new ScheduledTaskResponse(taskType.code(), taskType.name(), rr.value4(), rr.value3(), rr.value2());
            })
            .stream()
            .filter(x -> x != null)
            .toList();
    }

    private Condition getIdCondition(UUID id) {
        if (id == null) {
            return TASK.TASK_TYPE_ID.eq(TASK.TASK_TYPE_ID); // 1==1
        } else {
            return TASK.TASK_TYPE_ID.eq(id);
        }
    }

    public TaskConfigurationResponse getTaskConfiguration(UUID id) {
        var task = TASK.as("task");
        var taskType = TASK_TYPE.as("taskType");
        var configuration = TASK_CONFIG.as("configuration");
        var dependsOn = TASK_DEPENDENCIES.as("dependsOn");
        var isDependencyFor = TASK_DEPENDENCIES.as("isDependencyFor");
        var results =  dslContext
            .select(task.STATUS, configuration.CONFIG)
            .from(task)
            .leftJoin(configuration).on(task.ID.eq(configuration.TASK_ID))
            .where(task.ID.eq(id))
            .fetch(rr -> {
                var status = rr.value1();
                var config = rr.value2();

                var dependsOnV = dslContext
                    .select(dependsOn.REQUIRED_DEPENDENCY_TASK_ID, taskType.CODE)
                    .from(dependsOn)
                    .join(taskType).on(dependsOn.REQUIRED_DEPENDENCY_TASK_ID.eq(taskType.ID))
                    .where(dependsOn.TASK_ID.eq(id))
                    .fetch(rr2 -> new DependencyResponse(rr2.value1(), rr2.value2()));

                var isDependencyForV = dslContext
                    .select(isDependencyFor.TASK_ID, taskType.CODE)
                    .from(isDependencyFor)
                    .join(taskType).on(isDependencyFor.TASK_ID.eq(taskType.ID))
                    .where(isDependencyFor.REQUIRED_DEPENDENCY_TASK_ID.eq(id))
                    .fetch(rr2 -> new DependencyResponse(rr2.value1(), rr2.value2()));

                return new TaskConfigurationResponse(
                    id,
                    status,
                    config,
                    dependsOnV,
                    isDependencyForV
                );
            });


        return JqRepo.fetchOne(results);
    }

    public List<ExecutionResponse> getExecutions(UUID uuid) {
        var table =EFFECTIVE_HISTORIC_CONFIGURATION;
        return dslContext
            .select(table.STARTED_AT, table.FINISHED_AT, table.LOG, table.RESULT, table.ID, table.CONFIGURATION)
            .from(table)
            .where(table.TASK_ID.eq(uuid))
            .fetch(rr -> {
                return new ExecutionResponse(rr.component5(), rr.component4(), rr.component3(), rr.component1(), rr.component2(), rr.component6());
            });
    }

    @Transactional
    public Result2<Void, Void> override(UUID selectedTaskId, Set<UUID> preservedDependencies, TaskStatus status, String configuration) {
        if (status == TaskStatus.SCHEDULED || status == TaskStatus.IN_PROGRESS) {
            // those statuses depend on scheduler and will cause some issues
            return Result2.failure(null);
        }

        // has configuration actually changed?
        var currentConfiguration = dslContext
            .select(TASK_CONFIG.CONFIG)
            .from(TASK_CONFIG)
            .where(TASK_CONFIG.TASK_ID.eq(selectedTaskId))
            .fetchOne(0, String.class);

        boolean equal = (Strings.isBlank(currentConfiguration) && Strings.isBlank(configuration))
            || (Objects.equals(currentConfiguration, configuration));

        // if changed, let's update historical values
        if (!equal) {
            dslContext
                .update(TASK_EXECUTION)
                .set(TASK_EXECUTION.CONFIGURATION, configuration)
                .where(TASK_EXECUTION.TASK_ID.eq(selectedTaskId)).and(TASK_EXECUTION.FINISHED_AT.isNull())
                .execute();
        }

        // remove unused dependencies
        dslContext
            .delete(TASK_DEPENDENCIES)
            .where(TASK_DEPENDENCIES.TASK_ID.eq(selectedTaskId))
            .and(TASK_DEPENDENCIES.REQUIRED_DEPENDENCY_TASK_ID.notIn(preservedDependencies))
            .execute();

        // update status only if we are in a safe position to do so; otherwise we might be overriding a running task
        int updates = dslContext
            .update(TASK)
            .set(TASK.STATUS, status)
            .where(TASK.ID.eq(selectedTaskId))
            .and(TASK.STATUS.in(TaskStatus.CREATED, TaskStatus.DONE, TaskStatus.HARD_FAIL, TaskStatus.SOFT_FAIL, TaskStatus.DUPLICATE, TaskStatus.OVERRIDDEN_HARD_FAIL))
            .execute();

        if (updates != 1) { // apparently we weren't in a safe position to update; let's rollback the whole thing
            return Result2.failure(null);
        }
        return Result2.success(null);
    }

    public record ExecutionResponse(UUID id, TaskExecutionResult result, String log, OffsetDateTime startedAt, OffsetDateTime finishedAt, String configuration) {

        @Override
        public String toString() {
            return "[" + result + "] started @ " + startedAt + (finishedAt != null ? (", finished at: " + finishedAt) : "");
        }
    }

    public record ScheduledTaskResponse(String taskCode, String taskName, UUID taskId, LocalDateTime jobCreationTime, TaskStatus taskStatus) {


        @Override
        public String toString() {
            return "[" + taskId + "] " + taskName + " (" + taskCode + "): " + taskStatus + ", scheduled @ " + jobCreationTime;
        }
    }

    public record TaskConfigurationResponse(UUID taskId, TaskStatus status, String configuration, List<DependencyResponse> dependsOn, List<DependencyResponse> isDependencyFor) {

    }

    public record DependencyResponse(UUID taskId, String code) {

    }

}
