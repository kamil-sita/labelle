package place.sita.magicscheduler.scheduler;

import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.utils.ExceptionUtil;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.TaskTypeRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.count;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.jooq.Tables.*;
import static place.sita.labelle.jooq.tables.Task.TASK;

@Component
@Scope(scopeName = SCOPE_PROTOTYPE)
public class TypeSpecificQueue {

    private final TaskScheduler taskScheduler;

    private static final Logger log = LoggerFactory.getLogger(TypeSpecificQueue.class);
    private TaskType<?, ?, ?> taskType;
    private final DSLContext dslContext;
    private final Set<UUID> tasksInSchedulerOrExecutor = new HashSet<>();
    private int inSystemTargetCount = 100;
    private final MagicScheduler magicScheduler;
    private final ExecutionResultsSubmitter executionResultsSubmitter;
    private final TaskTypeRepository taskTypeRepository;
    private final SoftToHardFailPolicy softToHardFailPolicy;
    private final SchedulerProperties schedulerProperties;

    public TypeSpecificQueue(TaskScheduler taskScheduler,
                             DSLContext dslContext,
                             MagicScheduler magicScheduler,
                             ExecutionResultsSubmitter executionResultsSubmitter,
                             TaskTypeRepository taskTypeRepository,
                             SoftToHardFailPolicy softToHardFailPolicy,
                             SchedulerProperties schedulerProperties) {
        this.taskScheduler = taskScheduler;
        this.dslContext = dslContext;
        this.magicScheduler = magicScheduler;
        this.executionResultsSubmitter = executionResultsSubmitter;
        this.taskTypeRepository = taskTypeRepository;
        this.softToHardFailPolicy = softToHardFailPolicy;
	    this.schedulerProperties = schedulerProperties;
    }

    private boolean firstExecution = true;

    private void processFailedJobs() {
        var scheduledCondition = TASK.STATUS.in(TaskStatus.SCHEDULED)
            .and(TASK.TASK_TYPE_ID.eq(thisTaskTypeId()));

        var scheduledIds = dslContext
            .select(TASK.ID)
            .from(TASK)
            .where(scheduledCondition)
            .fetch()
            .map(Record1::value1);

        dslContext
            .update(TASK)
            .set(TASK.STATUS, TaskStatus.CREATED)
            .where(TASK.ID.in(scheduledIds))
            .execute();

        var startedCondition = TASK.STATUS.in(TaskStatus.IN_PROGRESS)
            .and(TASK.TASK_TYPE_ID.eq(thisTaskTypeId()));

        var inProgressIds = dslContext
            .select(TASK.ID)
            .from(TASK)
            .where(startedCondition)
            .fetch()
            .map(Record1::value1);

        var fails = countFailures(dslContext, inProgressIds);

        for (var id : inProgressIds) {
            var executions = fails.getOrDefault(id, 0);

            boolean willFail = softToHardFailPolicy.shouldFailHard(executions + 1);
            UUID executionId = UUID.randomUUID();
            ApiTaskExecutionResult finalResult = willFail ? ApiTaskExecutionResult.HARD_FAIL : ApiTaskExecutionResult.SOFT_FAIL;
            Instant executedAt = Instant.now();
            executionResultsSubmitter.submitResults(executionId, id, finalResult, "Execution scheduled but possibly not executed - job was moved from in progress after the application relaunch", ApiTaskExecutionResult.SOFT_FAIL, executedAt, executedAt, SCOPE_PROTOTYPE);
        }
    }

    public void scheduleFirstExecution() {
        scheduleNextExecution(inSystemTargetCount);
    }

    public void scheduleNextExecution(int fetch) {
        long maxDelay = 5000;
        long minDelay = 1000;
        long delay = minDelay + (long) ((maxDelay - minDelay) * (1.0 * fetch/inSystemTargetCount));

        Instant nextExecution = Instant.now().plus(delay, ChronoUnit.MILLIS);
        taskScheduler.schedule(this::fetch, nextExecution);
    }

    public void fetch() {
        int fetch = getTaskCountToFetch();

        if (schedulerProperties.isQueueEnabled()) {
            if (firstExecution) {
                processFailedJobs();
                firstExecution = false;
            } else {
                log.debug("Fetching for {}", taskType.code());

                fetchActual();
            }
        }

        scheduleNextExecution(fetch);
    }

    private void fetchActual() {
        int toFetchCount = getTaskCountToFetch();

        dslContext.transaction(inner -> {
            fetchInTransaction(inner, toFetchCount);
        });
    }

    private void fetchInTransaction(Configuration inner, int toFetchCount) {
        var jobsToSubmit = inner.dsl()
            .select(TASK_PLANNING.ID, TASK_PLANNING.CONFIG)
            .from(TASK_PLANNING)
            .where(TASK_PLANNING.TASK_TYPE_ID.eq(thisTaskTypeId()))
            .orderBy(TASK_PLANNING.CREATION_DATE)
            .limit(toFetchCount)
            .fetch()
            .map(rr -> {
                return new TaskWithConfig(rr.value1(), rr.value2());
            });

        var jobsToSubmitId = jobsToSubmit.stream().map(TaskWithConfig::id).collect(Collectors.toSet());

        if (jobsToSubmitId.isEmpty()) {
            return;
        }

        inner.dsl()
            .update(TASK)
            .set(TASK.STATUS, TaskStatus.SCHEDULED)
            .where(TASK.ID.in(jobsToSubmitId))
            .execute();

        var executionsByTask = countFailures(inner.dsl(), jobsToSubmitId);

        var readyToSubmitJobs = jobsToSubmit
            .stream()
            .map(taskWithConfig -> {
                var executions = executionsByTask.getOrDefault(taskWithConfig.id, 0);
                return new TaskWithConfigAndExecutionCount(taskWithConfig.id, taskWithConfig.config, executions);
            })
            .toList();

        tasksInSchedulerOrExecutor.addAll(jobsToSubmitId);
        List<JobExecFailures> failures = new ArrayList<>();
        for (var job : readyToSubmitJobs) {
            try {
                Object config = taskType.deserializeParam(job.config);
                magicScheduler.schedule(job.id, (TaskType) taskType, config, job.executionCount, () -> {
                    tasksInSchedulerOrExecutor.remove(job.id);
                });
            } catch (Exception e) {
                failures.add(new JobExecFailures(job.id, e));
            }
        }

        for (var failure : failures) {
            Instant execTime = Instant.now();
            String exception = ExceptionUtil.exceptionToString(failure.details);
            UUID executionId = UUID.randomUUID();
            executionResultsSubmitter.submitResults(executionId, failure.id, ApiTaskExecutionResult.HARD_FAIL, "Failed to execute due to a deserialization exception: \r\n" + exception, ApiTaskExecutionResult.HARD_FAIL, execTime, execTime, null);
        }
    }

    private static Map<UUID, Integer> countFailures(DSLContext dsl, Collection<UUID> jobsToSubmitId) {
        return dsl
            .select(TASK_EXECUTION.TASK_ID, count())
            .from(TASK_EXECUTION)
            .where(TASK_EXECUTION.TASK_ID.in(jobsToSubmitId))
            .groupBy(TASK_EXECUTION.TASK_ID)
            .fetch()
            .stream()
            .collect(toMap(Record2::value1, Record2::value2));
    }

    private int getTaskCountToFetch() {
        return Math.max(inSystemTargetCount - tasksInSchedulerOrExecutor.size(), 0);
    }

    private UUID thisTaskTypeId;

    private UUID thisTaskTypeId() {
        if (thisTaskTypeId == null) {
            thisTaskTypeId = taskTypeRepository.uuidByCode(taskType.code());
        }
        return thisTaskTypeId;
    }

    public void setType(TaskType<?, ?, ?> type) {
        this.taskType = type;
    }

    private record TaskWithConfig(UUID id, String config) {

    }

    private record JobExecFailures(UUID id, Exception details) {

    }

    private record TaskWithConfigAndExecutionCount(UUID id, String config, int executionCount) {

    }
}
