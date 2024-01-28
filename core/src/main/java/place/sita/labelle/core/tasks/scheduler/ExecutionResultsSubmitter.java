package place.sita.labelle.core.tasks.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

@Component
public class ExecutionResultsSubmitter {
    private final TaskStateRepository taskStateRepository;

    public ExecutionResultsSubmitter(TaskStateRepository taskStateRepository) {
        this.taskStateRepository = taskStateRepository;
    }

    @Transactional
    public void submitResults(UUID executionId, UUID taskId, ApiTaskExecutionResult finalTaskExecutionResult, String logs, ApiTaskExecutionResult executionResult, Instant start, Instant stop, String results) {
        taskStateRepository.assignState(taskId, mapToTaskResult(finalTaskExecutionResult));
        taskStateRepository.saveExecution(executionId, taskId, logs, mapToExecutionResult(executionResult), start, stop, results);
    }


    private TaskStatus mapToTaskResult(ApiTaskExecutionResult taskExecutionResult) {
        return switch (taskExecutionResult) {
            case DONE -> TaskStatus.DONE;
            case DUPLICATE -> TaskStatus.DUPLICATE;
            case SOFT_FAIL -> TaskStatus.SOFT_FAIL;
            case HARD_FAIL -> TaskStatus.HARD_FAIL;
        };
    }

    private TaskExecutionResult mapToExecutionResult(ApiTaskExecutionResult executionResult) {
        return switch (executionResult) {
            case DONE -> TaskExecutionResult.DONE;
            case DUPLICATE -> TaskExecutionResult.DUPLICATE;
            case SOFT_FAIL -> TaskExecutionResult.SOFT_FAIL;
            case HARD_FAIL -> TaskExecutionResult.HARD_FAIL;
        };
    }
}
