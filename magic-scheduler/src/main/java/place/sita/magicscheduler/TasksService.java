package place.sita.magicscheduler;

import org.springframework.stereotype.Service;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.tasktype.TaskTypeRegistry;
import place.sita.magicscheduler.transfer.KnownTaskTypesResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TasksService {

    private final TaskTypeRegistry taskTypeRegistry;
    private final InternalTaskSubmitter internalTaskSubmitter;

    public TasksService(TaskTypeRegistry taskTypeRegistry, InternalTaskSubmitter internalTaskSubmitter) {
        this.taskTypeRegistry = taskTypeRegistry;
        this.internalTaskSubmitter = internalTaskSubmitter;
    }

    public List<KnownTaskTypesResponse> getTaskTypes() {
        return taskTypeRegistry.all()
            .stream()
            .map(ttr -> {
                if (ttr instanceof TaskType<?, ?, ?> tt) {
                    return new KnownTaskTypesResponse(
                            tt.code(),
                            tt.name(),
                            new BigDecimal(-1), // todo deprecated?
                            tt.sampleValue(),
                            ttr.isHistoric()
                    );
                } else {
                    return new KnownTaskTypesResponse(
                            ttr.code(),
                            ttr.name(),
                            new BigDecimal(-1),
                            null,
                            ttr.isHistoric()
                    );
                }
            })
            .toList();
    }

    public UUID submitTask(String code, String parameters) {
        return internalTaskSubmitter.submitTaskForLater(code, parameters, InternalTaskSubmitter.UUID_FOR_USER_SUBMITTED_TASKS);
    }

}
