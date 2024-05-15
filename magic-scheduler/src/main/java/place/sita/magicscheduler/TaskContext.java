package place.sita.magicscheduler;

import place.sita.magicscheduler.scheduler.RunPolicy;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.UUID;

public interface TaskContext<AcceptedContextT> {

    <ParameterT, OtherAcceptedContextT, ReturnT> UUID submitAnotherTask(TaskType<ParameterT, OtherAcceptedContextT, ReturnT> task, ParameterT parameterT, RunPolicy runPolicy);

    void log(String parameter);

    TaskStateContext taskExecutionContext();

    <ResourceApiT> ResourceApiT forResource(Resource<ResourceApiT> resource);

    AcceptedContextT getApi();

}
