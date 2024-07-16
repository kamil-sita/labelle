package place.sita.magicscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.environment.SchedulerAwareTaskExecutionEnvironment;
import place.sita.magicscheduler.scheduler.events.TaskExecutionCompleteEvent;
import place.sita.magicscheduler.scheduler.events.TaskPickedUpEvent;
import place.sita.magicscheduler.scheduler.resources.LockResult;
import place.sita.magicscheduler.scheduler.resources.ResourceHub;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskTypeRef;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The MagicScheduler class is responsible for scheduling and executing tasks asynchronously.
 * It uses a ThreadPoolExecutor to manage the execution of tasks.
 */
@Component
public class MagicScheduler {
    private static final Logger log = LoggerFactory.getLogger(MagicScheduler.class);

    private final ScheduleLater scheduleLater;

    private final MagicSchedulerBackend magicSchedulerBackend;

    private final List<MagicSchedulerTask<?>> tasks = new ArrayList<>();
    private final SchedulerAwareTaskExecutionEnvironment executionEnvironment;
    private final ResourceHub resourceHub;
    private final ApplicationEventPublisher eventPublisher;

    public MagicScheduler(ScheduleLater scheduleLater,
                          MagicSchedulerBackend magicSchedulerBackend,
                          SchedulerAwareTaskExecutionEnvironment executionEnvironment,
                          ResourceHub resourceHub,
                          ApplicationEventPublisher eventPublisher) {
	    this.scheduleLater = scheduleLater;
	    this.magicSchedulerBackend = magicSchedulerBackend;
        this.executionEnvironment = executionEnvironment;
        this.resourceHub = resourceHub;
	    this.eventPublisher = eventPublisher;
    }

    public void schedule(UUID id, TaskTypeRef taskTypeRef, String config, int executionCount, ExecutionFinishedCallback callback) {
        if (taskTypeRef instanceof TaskType<?,?,?> taskType && !taskType.isHistoric()) {
            Object configDeserialized = taskType.deserializeParam(config);
            schedule(id, (TaskType) taskType, configDeserialized, executionCount, callback);
        } else {
            log.warn("Task with ID {} is scheduled, but cannot be executed by MagicScheduler", id);
        }
    }

    private <ParameterT> void schedule(UUID id, TaskType<ParameterT, ?, ?> task, ParameterT parameter, int executionCount, ExecutionFinishedCallback callback) {
        List<Resource<?>> resources = getResources(parameter, task);
        if (resources == null) {
            resources = List.of();
        }

        MagicSchedulerTask<ParameterT> magicSchedulerTask = new MagicSchedulerTask<>(
            executionCount,
            id,
            resources,
            task,
            parameter,
            callback
        );

        synchronized (this) {
            tasks.add(magicSchedulerTask);
            submitUnknownTaskToInternalScheduler();
        }

        eventPublisher.publishEvent(new TaskPickedUpEvent());
    }

    private static <T, U, R> List<Resource<?>> getResources(T parameter, TaskType<T, U, R> task) {
        return task.resources(parameter);
    }

    private void submitUnknownTaskToInternalScheduler() {
        magicSchedulerBackend.runLater(new UnknownTask());
    }

    private NextJobResult getJob() {
        synchronized (this) {
            if (tasks.isEmpty()) {
                // should this ever happen in practice?
                return new NoNextJob();
            } else {
                return findJobActual();
            }
        }
    }

    private NextJobResult findJobActual() {
        Map<Resource<?>, LockResult> checkedResources = new HashMap<>();

        Instant closest = Instant.MAX;

        tasksLoop:
        for (var task : new ArrayList<>(tasks)) {

            // can we obtain a lock on all resources?
            for (var resource : task.resources) {

                LockResult lockResult = checkedResources.get(resource);

                if (lockResult == null) {
                    lockResult = resourceHub.canBeLocked(resource);
                    checkedResources.put(resource, lockResult);

                    if (lockResult instanceof LockResult.LockFailRetryLaterAt retryLaterAt) {
                        if (retryLaterAt.suggestedTime().isBefore(closest)) {
                            closest = retryLaterAt.suggestedTime();
                        }
                    }
                }

                if (!lockResult.isSuccess()) {
                    // oops, not this task
                    continue tasksLoop;
                }

            }

            // apparently yes, so let's try it
            LockResult lock = resourceHub.tryLock(task.id, task.resources.toArray(Resource[]::new));

            if (lock.isSuccess()) {
                tasks.remove(task);
                return new Job(task);
            }

            // surprising. TODO Consider punishing task if it fails a lot.
        }

        Duration duration = Duration.between(Instant.now(), closest);
        Duration negativeCutoff = Duration.of(50, ChronoUnit.MILLIS);

        if (duration.compareTo(negativeCutoff) < 0) {
            return new NextJobNotAvailableYet(Instant.now().plus(negativeCutoff));
        }

        Duration positiveCutoff = Duration.of(1000, ChronoUnit.MILLIS);

        if (duration.compareTo(positiveCutoff) > 0) {
            return new NextJobNotAvailableYet(Instant.now().plus(positiveCutoff));
        }

        return new NextJobNotAvailableYet(Instant.now().plus(duration));

        // rip rat, the previous algorithm
    }

    public interface ExecutionFinishedCallback {
        void onExecutionFinished();
    }

    /*
     * PRIVATE CLASSES
     */

    private record MagicSchedulerTask<ParameterT>(
        int executionCount,
        UUID id,
        List<Resource<?>> resources,
        TaskType<ParameterT, ?, ?> taskType,
        ParameterT parameter,
        ExecutionFinishedCallback executionFinishedCallback) {
    }

    private class UnknownTask implements Runnable {

        @Override
        public void run() {
            // todo maybe the details of the implementation should be inside those task types?
            NextJobResult nextJob = getJob();
	        switch (nextJob) {
		        case NoNextJob noNextJob -> { /* nothing to do */ }
		        case NextJobNotAvailableYet nextJobNotAvailableYet -> {
			        scheduleLater.schedule(
				        MagicScheduler.this::submitUnknownTaskToInternalScheduler,
				        nextJobNotAvailableYet.expectedJobAvailabilityTime);
		        }
		        case Job job -> execute(job.task);
		        default -> {
                    throw new IllegalStateException("Unexpected value: " + nextJob);
                }
	        }
        }

        private <ParameterT> void execute(MagicSchedulerTask<ParameterT> task) {
            TaskType<ParameterT, ?, ?> type = task.taskType;
            ParameterT parameter = task.parameter;
            ApiTaskExecutionResult result = null;
            try {
                result = executionEnvironment.executeTask(task.id, type, parameter, task.executionCount);
            } catch (Throwable throwable) {
                log.error("Throwable while execution of task with ID " + task.id, throwable);
                throw throwable;
            } finally {
                resourceHub.unlock(task.id);
                task.executionFinishedCallback.onExecutionFinished();
                if (result != null) {
                    eventPublisher.publishEvent(new TaskExecutionCompleteEvent(result == ApiTaskExecutionResult.DONE || result == ApiTaskExecutionResult.DUPLICATE));
                } else {
                    eventPublisher.publishEvent(new TaskExecutionCompleteEvent(false));
                }
            }
        }
    }

    private sealed interface NextJobResult {

    }

    private static final class NoNextJob implements NextJobResult {

    }

    private record NextJobNotAvailableYet(Instant expectedJobAvailabilityTime) implements NextJobResult {
    }

    private record Job(MagicSchedulerTask task) implements NextJobResult {

    }
}
