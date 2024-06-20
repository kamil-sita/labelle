package place.sita.magicscheduler.scheduler.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.utils.ExceptionUtil;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.*;
import place.sita.magicscheduler.scheduler.resources.ResourceHub;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represents minimal, stateless (apart from dependencies) environment for task execution.
 */
@Component
public class TaskExecutionEnvironment {

	private static final Logger log = LoggerFactory.getLogger(TaskExecutionEnvironment.class);

	private final ApiRegistrar apiRegistrar;
	private final ResourceHub resourceHub;

	public TaskExecutionEnvironment(ApiRegistrar apiRegistrar, ResourceHub resourceHub) {
		this.apiRegistrar = apiRegistrar;
		this.resourceHub = resourceHub;
	}

	public <ParameterT, AcceptedContextT, ResultT> TaskExecutionResults<ResultT> executeTask(
		UUID internalExecutionId,
		TaskType<ParameterT, AcceptedContextT, ResultT> type,
		ParameterT parameter,
		TaskStateContext taskStateContext
	) {
		return executeTask(internalExecutionId, type, parameter, taskStateContext, apiRegistrar, resourceHub);
	}

	public static <ParameterT, AcceptedContextT, ResultT> TaskExecutionResults<ResultT> executeTask(
		UUID internalExecutionId,
		TaskType<ParameterT, AcceptedContextT, ResultT> type,
		ParameterT parameter,
		TaskStateContext taskStateContext,
		ApiRegistrar apiRegistrar,
		ResourceHub resourceHub
	) {
		log.info("Starting {}", internalExecutionId);

		List<TaskToSubmitWithRunPolicy> tasksToSubmit = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		AcceptedContextT api = apiRegistrar.resolve(type.contextType());

		TaskContext<AcceptedContextT> taskContext = new TaskContext<>() {
			private Logger log;
			@Override
			public <OtherParameterT, OtherAcceptedContextT, OtherResultT> UUID submitAnotherTask(TaskType<OtherParameterT, OtherAcceptedContextT, OtherResultT> task, OtherParameterT parameterT, RunPolicy runPolicy) {
				String stringParameter = task.serializeParam(parameterT);
				UUID uuid = UUID.randomUUID();
				log("Submitted another task with UUID: " + uuid);
				tasksToSubmit.add(new TaskToSubmitWithRunPolicy(uuid, task.code(), stringParameter, runPolicy));
				return uuid;
			}

			@Override
			public void log(String parameter) {
				if (log == null) {
					log = LoggerFactory.getLogger(type.getClass());
				}
				log.info(parameter);
				sb.append(Instant.now()).append(": ").append(parameter).append("\r\n");
			}

			@Override
			public TaskStateContext taskExecutionContext() {
				return taskStateContext;
			}

			@Override
			public <ResourceApiT> ResourceApiT forResource(Resource<ResourceApiT> resource) {
				return resourceHub.getApi(resource);
			}

			@Override
			public AcceptedContextT getApi() {
				return api;
			}
		};


		boolean failedDueToException = false;
		ApiTaskExecutionResult taskExecutionResult;
		TaskResult<ResultT> result;
		ResultT resultT = null;
		Throwable exc = null;
		String exception = null;
		String executionResultValue = null;
		try {
			result = executeActual(type, parameter, taskContext);
			taskExecutionResult = result.getStatus();
			if (result.getResult() != null) {
				resultT = result.getResult();
				executionResultValue = type.serializeResult(result.getResult());
			}
		} catch (Throwable e) {
			exc = e;
			// let's hope we don't fail here if it's an Error...
			failedDueToException = true;
			log.trace("Exception when executing {}", internalExecutionId, e);
			exception = ExceptionUtil.exceptionToString(e);
			taskExecutionResult = type.resolveThrowableIntoResult(e);
		} finally {
			log.info("Execution finished for {}", internalExecutionId);
		}
		if (exception != null) {
			sb.append("\r\n").append(exception);
		}

		ExecutionEnvironmentResult executionEnvironmentResult = new ExecutionEnvironmentResult(
			failedDueToException,
			taskExecutionResult == ApiTaskExecutionResult.HARD_FAIL,
			taskExecutionResult == ApiTaskExecutionResult.DUPLICATE,
			taskExecutionResult == ApiTaskExecutionResult.SOFT_FAIL,
			taskExecutionResult == ApiTaskExecutionResult.DONE
		);

		return new TaskExecutionResults<>(
			taskExecutionResult,
			failedDueToException,
			resultT,
			executionResultValue, // todo should we do serialization in this layer? What if serialization fails?
			tasksToSubmit.stream().filter(t -> t.runPolicy().shouldRun(executionEnvironmentResult)).map(t -> new TaskToSubmit(t.id, t.code, t.parameter)).toList(),
			exc,
			sb.toString()
		);
	}

	private static <T, U, R> TaskResult<R> executeActual(TaskType<T, U, R> type, T parameter, TaskContext<U> taskContext) {
		return type.runTask(parameter, taskContext);
	}

	private record TaskToSubmitWithRunPolicy(
		UUID id,
		String code,
		String parameter,
		RunPolicy runPolicy
	) {
	}
}
