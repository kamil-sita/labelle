package place.sita.magicscheduler.tasktype.annotationdef;

import org.springframework.stereotype.Component;

import place.sita.magicscheduler.scheduler.BaseTaskSandbox;

@Component
public class AnnotationExampleTask {


	@MsTask(code = "example-annotation-task", name = "Example annotation task")
	public void execute(@MsParameter Parameter parameter, @MsSandbox BaseTaskSandbox sandbox) {
		sandbox.log("Hello world! \"" + parameter + "\"");
	}

	public record Parameter(String value) {

	}

}
