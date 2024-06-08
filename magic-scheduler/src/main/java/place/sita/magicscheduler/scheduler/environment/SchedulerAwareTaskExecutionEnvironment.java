package place.sita.magicscheduler.scheduler.environment;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.InternalTaskSubmitter;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.*;

import java.time.Instant;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.TASK;

@Component
public class SchedulerAwareTaskExecutionEnvironment {
    private static final Logger log = LoggerFactory.getLogger(SchedulerAwareTaskExecutionEnvironment.class);


    private final InternalTaskSubmitter internalTaskSubmitter;
    private final TaskStateRepository taskStateRepository;
    private final SoftToHardFailPolicy softToHardFailPolicy;
    private final PlatformTransactionManager platformTransactionManager;
    private final ExecutionResultsSubmitter executionResultsSubmitter;
    private final TaskExecutionEnvironment taskExecutionEnvironment;
    private final DSLContext dslContext;

    public SchedulerAwareTaskExecutionEnvironment(
	    InternalTaskSubmitter internalTaskSubmitter,
	    TaskStateRepository taskStateRepository,
	    SoftToHardFailPolicy softToHardFailPolicy,
	    PlatformTransactionManager platformTransactionManager,
	    ExecutionResultsSubmitter executionResultsSubmitter,
	    TaskExecutionEnvironment taskExecutionEnvironment,
        DSLContext dslContext) {
        this.internalTaskSubmitter = internalTaskSubmitter;
        this.taskStateRepository = taskStateRepository;
        this.softToHardFailPolicy = softToHardFailPolicy;
        this.platformTransactionManager = platformTransactionManager;
        this.executionResultsSubmitter = executionResultsSubmitter;
	    this.taskExecutionEnvironment = taskExecutionEnvironment;
	    this.dslContext = dslContext;
    }

    public <ParameterT, AcceptedContextT, ResultT> ApiTaskExecutionResult executeTask(UUID taskId, TaskType<ParameterT, AcceptedContextT, ResultT> type, ParameterT parameter, int failsSoFar) {
        boolean exists = dslContext
            .fetchExists(TASK, TASK.ID.eq(taskId));
        if (!exists) {
            log.error("Task {} does not exist in the database", taskId);
        }
        taskStateRepository.assignState(taskId, TaskStatus.IN_PROGRESS);

        Instant start = Instant.now();

        TaskExecutionResults<ResultT> ter = taskExecutionEnvironment.executeTask(
            taskId,
            type,
            parameter,
            new TaskStateContext(failsSoFar > 0)
        );

        Instant stop = Instant.now();

        boolean willCauseHardFail = false;
        ApiTaskExecutionResult taskExecutionResult = ter.taskExecutionResult();

        if (taskExecutionResult == null) {
            taskExecutionResult = ApiTaskExecutionResult.DONE;
        }
        if (taskExecutionResult == ApiTaskExecutionResult.SOFT_FAIL) {
            if (softToHardFailPolicy.shouldFailHard(failsSoFar + 1)) {
                willCauseHardFail = true;
            }
        }

        ApiTaskExecutionResult executionResult = taskExecutionResult;
        if (willCauseHardFail) {
            taskExecutionResult = ApiTaskExecutionResult.HARD_FAIL;
        }

        ApiTaskExecutionResult finalTaskExecutionResult = taskExecutionResult;

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        String finalExecutionResultValue = ter.resultSerialized();
        transactionTemplate.execute(status -> {
            UUID executionId = UUID.randomUUID();

            for (var taskToSubmit : ter.tasksToSubmit()) {
                internalTaskSubmitter.submitTaskForLater(taskToSubmit.id(), taskToSubmit.code(), taskToSubmit.parameter(), executionId);
            }

            executionResultsSubmitter.submitResults(
                executionId,
                taskId,
                finalTaskExecutionResult,
                ter.logs(),
                executionResult,
                start,
                stop,
                finalExecutionResultValue);

            return null;
        });


        if (ter.exception() instanceof Error error) {
            throw error;
        }

        return finalTaskExecutionResult;
    }
}
