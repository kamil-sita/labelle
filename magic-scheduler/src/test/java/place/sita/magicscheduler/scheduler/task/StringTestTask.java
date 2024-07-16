package place.sita.magicscheduler.scheduler.task;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;

@Component
public class StringTestTask implements TaskType<String, Void, String> {

	private final StringTaskTestValueProcessor stringTaskTestValueProcessor;

	public StringTestTask(StringTaskTestValueProcessor stringTaskTestValueProcessor) {
		this.stringTaskTestValueProcessor = stringTaskTestValueProcessor;
	}

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
		return stringTaskTestValueProcessor.process(parameter, taskContext);
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
