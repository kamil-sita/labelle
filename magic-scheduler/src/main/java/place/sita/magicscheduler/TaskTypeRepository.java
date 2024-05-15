package place.sita.magicscheduler;


import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static place.sita.labelle.jooq.tables.TaskType.TASK_TYPE;

@Component
public class TaskTypeRepository {

    private static final Logger log = LoggerFactory.getLogger(TaskTypeRepository.class);

    private final DSLContext ctx;
    private final List<TaskType<?, ?, ?>> taskTypeDefinitions;
    private Map<String, TaskType<?, ?, ?>> tasksByCode;
    private Map<UUID, TaskType<?, ?, ?>> tasksByUUID;
    private Map<String, UUID> uuidByCode;


    @PostConstruct
    public void syncDatabaseDefinitions() {
        Set<String> uniqueKnownTaskCodes = new HashSet<>();
        tasksByUUID = new HashMap<>();
        tasksByCode = new HashMap<>();
        uuidByCode = new HashMap<>();

        for (var taskDefinition : taskTypeDefinitions) {
            if (!uniqueKnownTaskCodes.add(taskDefinition.code())) {
                throw new IllegalStateException("Expected task codes to be unique, but " + taskDefinition + " duplicates code " + taskDefinition.code());
            }
            tasksByCode.put(taskDefinition.code(), taskDefinition);
        }


        log.info("Known tasks: {}", uniqueKnownTaskCodes);

        var results = ctx
            .select(TASK_TYPE.ID, TASK_TYPE.CODE)
            .from(TASK_TYPE)
            .fetch();

        for (var result : results) {
            tasksByUUID.put(result.component1(), tasksByCode.get(result.component2()));
            uuidByCode.put(result.component2(), result.value1());
        }

        Set<String> codesInDb = results.intoSet(TASK_TYPE.CODE);

        Set<String> codesNotInDb = Sets.difference(uniqueKnownTaskCodes, codesInDb);

        for (String code : codesNotInDb) {
            UUID uuid = UUID.randomUUID();
            TaskType<?, ?, ?> taskType = tasksByCode.get(code);
            tasksByUUID.put(uuid, taskType);
            uuidByCode.put(code, uuid);

            ctx
                .insertInto(TASK_TYPE)
                .columns(TASK_TYPE.ID, TASK_TYPE.CODE, TASK_TYPE.NAME)
                .values(uuid, taskType.code(), taskType.name())
                .execute();
        }
    }

    public TaskTypeRepository(DSLContext ctx, List<TaskType<?, ?, ?>> taskTypeDefinitions) {
        this.ctx = ctx;
        this.taskTypeDefinitions = taskTypeDefinitions;
    }

    public List<TaskType<?, ?, ?>> all() {
        return new ArrayList<>(taskTypeDefinitions);
    }

    public List<TaskTypeResponse> allR() {
        return all().stream().map(tt -> new TaskTypeResponse(tt.code(), tt.name(), uuidByCode(tt.code()))).toList();
    }

    public TaskType<?, ?, ?> byCode(String code) {
        return tasksByCode.get(code);
    }

    public UUID uuidByCode(String code) {
        return uuidByCode.get(code);
    }

    public TaskType<?, ?, ?> byUUID(UUID uuid) {
        return tasksByUUID.get(uuid);
    }

    public record TaskTypeWithUuid(TaskType<?, ?, ?> taskType, UUID id) {

    }

    public record TaskTypeResponse(String code, String name, UUID id) {

        @Override
        public String toString() {
            return name + " (" + code + ")";
        }
    }

}
