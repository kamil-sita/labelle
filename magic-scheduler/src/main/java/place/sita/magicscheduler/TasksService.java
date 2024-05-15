package place.sita.magicscheduler;

import org.springframework.stereotype.Service;
import place.sita.labelle.core.tasks.transfer.KnownTaskTypesResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TasksService {

    private final TaskTypeRepository taskTypeRepository;
    private final InternalTaskSubmitter internalTaskSubmitter;

    public TasksService(TaskTypeRepository taskTypeRepository, InternalTaskSubmitter internalTaskSubmitter) {
        this.taskTypeRepository = taskTypeRepository;
        this.internalTaskSubmitter = internalTaskSubmitter;
    }

    public List<KnownTaskTypesResponse> getTaskTypes() {
        return taskTypeRepository.all()
            .stream()
            .map(tt -> {
                return new KnownTaskTypesResponse(
                    tt.code(),
                    tt.name(),
                    new BigDecimal(-1),
                    tt.sampleValue()
                );
            })
            .toList();
    }

    public UUID submitTask(String code, String parameters) {
        return internalTaskSubmitter.submitTaskForLater(code, parameters, InternalTaskSubmitter.UUID_FOR_USER_SUBMITTED_TASKS);
    }

}
