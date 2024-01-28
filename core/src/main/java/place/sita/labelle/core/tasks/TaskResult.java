package place.sita.labelle.core.tasks;

import place.sita.labelle.core.tasks.scheduler.ApiTaskExecutionResult;

public class TaskResult<T> {

	private final ApiTaskExecutionResult status;
	private final T result;

	private TaskResult(ApiTaskExecutionResult status, T result) {
		this.status = status;
		this.result = result;
	}

	public static <T> TaskResult<T> success() {
		return new TaskResult<>(ApiTaskExecutionResult.DONE, null);
	}

	public static <T> TaskResult<T> success(T result) {
		return new TaskResult<>(ApiTaskExecutionResult.DONE, result);
	}

	public static <T> TaskResult<T> duplicate(T result) {
		return new TaskResult<>(ApiTaskExecutionResult.DUPLICATE, result);
	}

	public static <T> TaskResult<T> duplicate() {
		return new TaskResult<>(ApiTaskExecutionResult.DUPLICATE, null);
	}

	public static <T> TaskResult<T> of(ApiTaskExecutionResult status) {
		return new TaskResult<>(status, null);
	}

	public ApiTaskExecutionResult getStatus() {
		return status;
	}

	public T getResult() {
		return result;
	}
}
