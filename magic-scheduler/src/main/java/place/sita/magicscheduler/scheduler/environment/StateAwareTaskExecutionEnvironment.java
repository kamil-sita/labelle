package place.sita.magicscheduler.scheduler.environment;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.ApiTaskExecutionResult;
import place.sita.magicscheduler.scheduler.SoftToHardFailPolicy;
import place.sita.magicscheduler.scheduler.TaskStateContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class StateAwareTaskExecutionEnvironment {

	private final TaskExecutionEnvironment taskExecutionEnvironment;
	private final SoftToHardFailPolicy softToHardFailPolicy;

	public StateAwareTaskExecutionEnvironment(TaskExecutionEnvironment taskExecutionEnvironment, SoftToHardFailPolicy softToHardFailPolicy) {
		this.taskExecutionEnvironment = taskExecutionEnvironment;
		this.softToHardFailPolicy = softToHardFailPolicy;
	}

	public <ParameterT, AcceptedContextT, ResultT> Results executeTask(UUID taskId, TaskType<ParameterT, AcceptedContextT, ResultT> type, ParameterT parameter, int failsSoFar) {
		Instant start = Instant.now();

		TaskExecutionResults<ResultT> ter = taskExecutionEnvironment.executeTask(
			taskId,
			type,
			parameter,
			new TaskStateContext(failsSoFar > 0)
		);

		Instant stop = Instant.now();

		boolean willCauseHardFail = false;
		ApiTaskExecutionResult taskRunExecutionResult = ter.taskExecutionResult();

		if (taskRunExecutionResult == null) {
			taskRunExecutionResult = ApiTaskExecutionResult.DONE;
		}
		if (taskRunExecutionResult == ApiTaskExecutionResult.SOFT_FAIL) {
			if (softToHardFailPolicy.shouldFailHard(failsSoFar + 1)) {
				willCauseHardFail = true;
			}
		}

		ApiTaskExecutionResult taskExecutionResult = taskRunExecutionResult;
		if (willCauseHardFail) {
			taskRunExecutionResult = ApiTaskExecutionResult.HARD_FAIL;
		}

		return new Results(
			taskRunExecutionResult,
			ter.resultSerialized(),
			ter.logs(),
			taskExecutionResult,
			start,
			stop,
			ter.tasksToSubmit(),
			ter.exception()
		);
	}

	public record Results (
		ApiTaskExecutionResult taskRunExecutionResult,
		String result,
		String logs,
		ApiTaskExecutionResult taskExecutionResult,
		Instant start,
		Instant stop,
		List<TaskToSubmit> tasksToSubmit,
		Throwable exception) {}

}
