package place.sita.magicscheduler.scheduler.task;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;

@Component
public class StringTaskTestValueProcessor {

	public TaskResult<String> process(String parameter, TaskContext<Void> taskContext) {
		return TaskResult.success("Success: " + parameter);
	}

}
