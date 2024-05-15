package place.sita.magicscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.TaskTypeRepository;
import place.sita.magicscheduler.scheduler.environment.SchedulerAwareTaskExecutionEnvironment;
import place.sita.magicscheduler.scheduler.events.TaskExecutionCompleteEvent;
import place.sita.magicscheduler.scheduler.events.TaskPickedUpEvent;
import place.sita.magicscheduler.scheduler.resources.LockResult;
import place.sita.magicscheduler.scheduler.resources.ResourceHub;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
 * The MagicScheduler class is responsible for scheduling and executing tasks asynchronously.
 * It uses a ThreadPoolExecutor to manage the execution of tasks.
 */
@Component
public class MagicScheduler {
    private static final Logger log = LoggerFactory.getLogger(MagicScheduler.class);

    private final TaskScheduler taskScheduler;

    private final ThreadPoolExecutor executorService;

    private final List<MagicSchedulerTask> tasks = new ArrayList<>();
    private final TaskTypeRepository taskTypeRepository;
    private final SchedulerAwareTaskExecutionEnvironment executionEnvironment;
    private final ResourceHub resourceHub;
    private final ApplicationEventPublisher eventPublisher;

    public MagicScheduler(TaskScheduler taskScheduler,
                          TaskTypeRepository taskTypeRepository,
                          SchedulerAwareTaskExecutionEnvironment executionEnvironment,
                          ResourceHub resourceHub,
                          ApplicationEventPublisher eventPublisher) {
        this.taskScheduler = taskScheduler;
        this.taskTypeRepository = taskTypeRepository;
        this.executionEnvironment = executionEnvironment;
        this.resourceHub = resourceHub;
	    this.eventPublisher = eventPublisher;
	    executorService = new ThreadPoolExecutor(
            1,
            4,
            2000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
        );
        executorService.allowCoreThreadTimeOut(true);
    }

    //@Scheduled(fixedDelay = 2000)
    public void optimize() {
        // todo
    }

    public void schedule(UUID id, String code, Object parameter, int executionCount, ExecutionFinishedCallback callback) {
        Instant submitted = Instant.now();

        TaskType task = taskTypeRepository.byCode(code);
        List<Resource<?>> resources = getResources(parameter, task);
        if (resources == null) {
            resources = List.of();
        }

        MagicSchedulerTask magicSchedulerTask = new MagicSchedulerTask(
            executionCount,
            id,
            resources,
            code,
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
        executorService.submit(new UnknownTask());
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

    private record MagicSchedulerTask(
        int executionCount,
        UUID id,
        List<Resource<?>> resources,
        String code,
        Object parameter,
        ExecutionFinishedCallback executionFinishedCallback) {
    }

    private class UnknownTask implements Runnable {

        @Override
        public void run() {
            // todo maybe the details of the implementation should be inside those task types?
            NextJobResult nextJob = getJob();
            if (nextJob instanceof NoNextJob) {
                return;
            }
            if (nextJob instanceof NextJobNotAvailableYet nextJobNotAvailableYet) {
                taskScheduler.schedule(
                    MagicScheduler.this::submitUnknownTaskToInternalScheduler,
                    nextJobNotAvailableYet.expectedJobAvailabilityTime);
                return;
            }
            if (nextJob instanceof Job job) {
                execute(job.task);
            }
        }

        private void execute(MagicSchedulerTask task) {
            TaskType<?, ?, ?> type = taskTypeRepository.byCode(task.code);
            Object parameter = task.parameter;
            ApiTaskExecutionResult result = null;
            try {
                result = executionEnvironment.executeTask(task.id, (TaskType) type, (Object) parameter, task.executionCount);
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
