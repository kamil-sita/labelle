package place.sita.labelle.core.tasks.scheduler.environment;

import place.sita.labelle.core.tasks.scheduler.ApiTaskExecutionResult;

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
