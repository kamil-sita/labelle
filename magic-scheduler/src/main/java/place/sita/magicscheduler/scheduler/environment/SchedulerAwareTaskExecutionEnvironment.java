package place.sita.magicscheduler.scheduler.environment;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.InternalTaskSubmitter;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.*;

import java.util.UUID;

import static place.sita.labelle.jooq.Tables.TASK;

@Component
public class SchedulerAwareTaskExecutionEnvironment {

    private final InternalTaskSubmitter internalTaskSubmitter;
    private final TaskStateRepository taskStateRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final ExecutionResultsSubmitter executionResultsSubmitter;
    private final DSLContext dslContext;

    private final StateAwareTaskExecutionEnvironment stateAwareTaskExecutionEnvironment;

    public SchedulerAwareTaskExecutionEnvironment(
	    InternalTaskSubmitter internalTaskSubmitter,
	    TaskStateRepository taskStateRepository,
	    PlatformTransactionManager platformTransactionManager,
	    ExecutionResultsSubmitter executionResultsSubmitter,
	    DSLContext dslContext,
        StateAwareTaskExecutionEnvironment stateAwareTaskExecutionEnvironment) {
        this.internalTaskSubmitter = internalTaskSubmitter;
        this.taskStateRepository = taskStateRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.executionResultsSubmitter = executionResultsSubmitter;
	    this.dslContext = dslContext;
	    this.stateAwareTaskExecutionEnvironment = stateAwareTaskExecutionEnvironment;
    }

    public <ParameterT, AcceptedContextT, ResultT> ApiTaskExecutionResult executeTask(UUID taskId, TaskType<ParameterT, AcceptedContextT, ResultT> type, ParameterT parameter, int failsSoFar) {
        boolean exists = dslContext.fetchExists(TASK, TASK.ID.eq(taskId));
        if (!exists) {
            throw new InvalidStateException("Task \"" + taskId + "\" does not exist in the database");
        }
        taskStateRepository.assignState(taskId, TaskStatus.IN_PROGRESS);

        StateAwareTaskExecutionEnvironment.Results results = stateAwareTaskExecutionEnvironment.executeTask(taskId, type, parameter, failsSoFar);

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        String finalExecutionResultValue = results.result();

        transactionTemplate.execute(status -> {
            UUID executionId = UUID.randomUUID();

            for (var taskToSubmit : results.tasksToSubmit()) {
                internalTaskSubmitter.submitTaskForLater(taskToSubmit.id(), taskToSubmit.code(), taskToSubmit.parameter(), executionId);
            }

            executionResultsSubmitter.submitResults(
                executionId,
                taskId,
                results.taskExecutionResult(),
                results.logs(),
                results.taskRunExecutionResult(),
                results.start(),
                results.stop(),
                finalExecutionResultValue);

            return null;
        });


        if (results.exception() instanceof Error error) {
            throw error;
        }

        return results.taskExecutionResult();
    }

    private static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }
}
