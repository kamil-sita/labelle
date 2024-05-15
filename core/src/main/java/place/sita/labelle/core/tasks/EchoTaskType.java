package place.sita.labelle.core.tasks;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;

/**
 * Sample task that only writes back whatever was put into it.
 */
@Component
public class EchoTaskType implements TaskType<String, Void, EchoTaskType.StringHolder> {
    @Override
    public String code() {
        return "echo";
    }

    @Override
    public String name() {
        return "Echo";
    }

    @Override
    public TaskResult<StringHolder> runTask(String parameter, TaskContext taskContext) {
        taskContext.log(parameter);
        return TaskResult.success(new StringHolder(parameter));
    }

    @Override
    public String deserializeParam(String s) {
        return s;
    }

    @Override
    public String serializeParam(String s) {
        return s;
    }

    @Override
    public String sampleValue() {
        return "Echo";
    }

    @Override
    public List<Resource<?>> resources(String s) {
        return List.of();
    }

    @Override
    public Class<Void> contextType() {
        return null;
    }

    @Override
    public Class<String> paramType() {
        return String.class;
    }

    @Override
    public Class<StringHolder> resultType() {
        return StringHolder.class;
    }

    public record StringHolder (String s) {

    }
}
