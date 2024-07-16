package place.sita.magicscheduler.scheduler;

import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.utils.ExceptionUtil;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.tasktype.TaskTypeRef;
import place.sita.magicscheduler.tasktype.TaskTypeRegistry;

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

    private final ScheduleLater scheduleLater;

    private static final Logger log = LoggerFactory.getLogger(TypeSpecificQueue.class);
    private TaskTypeRef taskType;
    private final DSLContext dslContext;
    private final Set<UUID> tasksInSchedulerOrExecutor = new HashSet<>();
    private int inSystemTargetCount = 100;
    private final MagicScheduler magicScheduler;
    private final ExecutionResultsSubmitter executionResultsSubmitter;
    private final SoftToHardFailPolicy softToHardFailPolicy;
    private final SchedulerProperties schedulerProperties;

    public TypeSpecificQueue(ScheduleLater scheduleLater,
                             DSLContext dslContext,
                             MagicScheduler magicScheduler,
                             ExecutionResultsSubmitter executionResultsSubmitter,
                             SoftToHardFailPolicy softToHardFailPolicy,
                             SchedulerProperties schedulerProperties) {
        this.scheduleLater = scheduleLater;
        this.dslContext = dslContext;
        this.magicScheduler = magicScheduler;
        this.executionResultsSubmitter = executionResultsSubmitter;
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
        if (!schedulerProperties.isQueueEnabled()) {
            return;
        }
        long maxDelay = 5000;
        long minDelay = 1000;
        long delay = minDelay + (long) ((maxDelay - minDelay) * (1.0 * fetch/inSystemTargetCount));

        Instant nextExecution = Instant.now().plus(delay, ChronoUnit.MILLIS);
        scheduleLater.schedule(this::scheduledFetch, nextExecution);
    }

    private void scheduledFetch() {
        int fetch = getTaskCountToFetch();

        if (schedulerProperties.isQueueEnabled()) {
            if (firstExecution) {
                processFailedJobs();
                firstExecution = false;
            } else {
                log.debug("Fetching for {}", taskType.code());

                scheduledFetchActual();
            }
        }

        scheduleNextExecution(fetch);
    }

    private void scheduledFetchActual() {
        synchronized (schedulingLock) {
            int toFetchCount = getTaskCountToFetch();

            dslContext.transaction(inner -> {
                scheduledFetchInTransaction(inner, toFetchCount);
            });
        }
    }

    private final Object schedulingLock = new Object();

    private void scheduledFetchInTransaction(Configuration inner, int toFetchCount) {
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

        submitTasksWithConfig(inner, jobsToSubmit);
    }

    private void scheduleWithoutQueue(UUID id, String config) {
        dslContext.transaction(inner -> {
            submitTasksWithConfig(inner, List.of(new TaskWithConfig(id, config)));
        });
    }

    public void doInSchedulingLock(InSchedulingLock r) {
        Context context = new Context() {
            @Override
            public void scheduleWithoutQueue(UUID id, String config) {
                TypeSpecificQueue.this.scheduleWithoutQueue(id, config);
            }
        };
        synchronized (schedulingLock) {
            r.run(context);
        }
    }

    public interface Context {
        void scheduleWithoutQueue(UUID id, String config);
    }

    public interface InSchedulingLock {
        void run(Context ctx);
    }

    private void submitTasksWithConfig(Configuration inner, List<TaskWithConfig> jobsToSubmit) {
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
                magicScheduler.schedule(job.id, taskType, job.config, job.executionCount, () -> {
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

    public void setThisTaskTypeId(UUID thisTaskTypeId) {
        this.thisTaskTypeId = thisTaskTypeId;
    }

    private UUID thisTaskTypeId() {
        return thisTaskTypeId;
    }

    public void setType(TaskTypeRef type) {
        this.taskType = type;
    }

    private record TaskWithConfig(UUID id, String config) {

    }

    private record JobExecFailures(UUID id, Exception details) {

    }

    private record TaskWithConfigAndExecutionCount(UUID id, String config, int executionCount) {

    }
}
