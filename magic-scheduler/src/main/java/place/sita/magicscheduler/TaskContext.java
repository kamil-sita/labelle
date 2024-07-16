package place.sita.magicscheduler;

import place.sita.magicscheduler.scheduler.BaseTaskSandbox;
import place.sita.magicscheduler.scheduler.RunPolicy;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.util.UUID;

public interface TaskContext<AcceptedContextT> extends BaseTaskSandbox {

    AcceptedContextT getApi();

}
