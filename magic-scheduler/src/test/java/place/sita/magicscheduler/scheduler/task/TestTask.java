package place.sita.magicscheduler.scheduler.task;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;

@Component
public class TestTask implements TaskType<String, Void, String> {
	@Override
	public String code() {
		return "test-task";
	}

	@Override
	public String name() {
		return "Test task";
	}

	@Override
	public TaskResult<String> runTask(String parameter, TaskContext<Void> taskContext) {
		return TaskResult.success("Success: " + parameter);
	}

	@Override
	public String sampleValue() {
		return "Sample";
	}

	@Override
	public List<Resource<?>> resources(String s) {
		return List.of();
	}

	@Override
	public Class<Void> contextType() {
		return Void.class;
	}

	@Override
	public Class<String> paramType() {
		return String.class;
	}

	@Override
	public Class<String> resultType() {
		return String.class;
	}
}
