package place.sita.magicscheduler.scheduler;

import java.util.UUID;

import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

public interface BaseTaskSandbox {

	<ParameterT, AcceptedContextT, ReturnT> UUID submitAnotherTask(
			TaskType<ParameterT, AcceptedContextT, ReturnT> task, ParameterT parameterT, RunPolicy runPolicy);

	UUID submitAnotherTask(String code, Object parameter, RunPolicy runPolicy);

	// todo MessageFormatter slf4j style
	void log(String parameter);

	TaskStateContext taskExecutionContext();

	<ResourceApiT> ResourceApiT forResource(Resource<ResourceApiT> resource);
}
