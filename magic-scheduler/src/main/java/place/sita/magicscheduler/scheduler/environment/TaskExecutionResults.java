package place.sita.magicscheduler.scheduler.environment;

import place.sita.magicscheduler.scheduler.ApiTaskExecutionResult;

import java.util.List;

public record TaskExecutionResults<ResultT>(
	ApiTaskExecutionResult taskExecutionResult,
	boolean failedDueToException,
	ResultT result,
	String resultSerialized,
	List<TaskToSubmit> tasksToSubmit,
	Throwable exception,
	String logs
) {

}
