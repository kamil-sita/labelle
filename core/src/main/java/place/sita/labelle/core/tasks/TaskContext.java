package place.sita.labelle.core.tasks;

import place.sita.labelle.core.tasks.scheduler.RunPolicy;
import place.sita.labelle.core.tasks.scheduler.TaskStateContext;
import place.sita.labelle.core.tasks.scheduler.resources.resource.Resource;

import java.util.UUID;

public interface TaskContext<AcceptedContextT> {

    <ParameterT, OtherAcceptedContextT, ReturnT> UUID submitAnotherTask(TaskType<ParameterT, OtherAcceptedContextT, ReturnT> task, ParameterT parameterT, RunPolicy runPolicy);

    void log(String parameter);

    TaskStateContext taskExecutionContext();

    <ResourceApiT> ResourceApiT forResource(Resource<ResourceApiT> resource);

    AcceptedContextT getApi();

}
