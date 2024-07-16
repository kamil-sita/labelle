package place.sita.magicscheduler.tasktype;

import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import place.sita.magicscheduler.SerializerRegistry;
import place.sita.magicscheduler.scheduler.TypeSpecificQueue;
import place.sita.magicscheduler.scheduler.TypeSpecificQueueRegistry;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static place.sita.labelle.jooq.tables.TaskType.TASK_TYPE;

@Component
public class TaskTypeRegistry {

    private static final Logger log = LoggerFactory.getLogger(TaskTypeRegistry.class);

    private final DSLContext ctx;
    private final List<TaskTypeRef> taskTypeDefinitions;
    private final Map<String, TaskTypeRef> tasksByCode = new HashMap<>();
    private final Map<UUID, TaskTypeRef> tasksByUUID = new HashMap<>();
    private final Map<String, UUID> uuidByCode = new HashMap<>();
    private final ApplicationContext applicationContext;
    private final TypeSpecificQueueRegistry typeSpecificQueueRegistry;
    private final SerializerRegistry serializerRegistry;

    public TaskTypeRegistry(DSLContext ctx,
            List<TaskTypeRef> taskTypeDefinitions,
			ApplicationContext applicationContext,
			TypeSpecificQueueRegistry typeSpecificQueueRegistry,
            SerializerRegistry serializerRegistry) {
        this.ctx = ctx;
        this.taskTypeDefinitions = taskTypeDefinitions;
		this.applicationContext = applicationContext;
		this.typeSpecificQueueRegistry = typeSpecificQueueRegistry;
		this.serializerRegistry = serializerRegistry;
	}

    @PostConstruct
    public void syncDatabaseDefinitions() {
        register(taskTypeDefinitions);
    }

    public void register(List<TaskTypeRef> refs) {
        registerNewTasks(refs);

        updateUuidMappings();

        createNewQueues(refs);
    }

    private void registerNewTasks(List<TaskTypeRef> refs) {
        for (var taskDefinition : refs) {
            TaskTypeRef ref = tasksByCode.get(taskDefinition.code());
            if (ref == null || ref.isHistoric()) {
                tasksByCode.put(taskDefinition.code(), taskDefinition);
                if (!taskDefinition.isHistoric() && taskDefinition instanceof TaskType tt) {
                    serializerRegistry.register(taskDefinition.code(), x -> tt.serializeParam(x));
                }
            } else {
                throw new IllegalStateException("Expected task codes to be unique, but " + taskDefinition + " duplicates code " + taskDefinition.code());
            }
        }

        Set<String> tasksToRegister = refs.stream().map(TaskTypeRef::code).collect(toSet());

        log.info("Registering tasks: {}", tasksToRegister);
    }

    private void updateUuidMappings() {
        // note: this is not optimal, as it always refetches everything, but it shouldn't be a problem short-term
        var results = ctx
            .select(TASK_TYPE.ID, TASK_TYPE.CODE)
            .from(TASK_TYPE)
            .fetch();

        for (var result : results) {
            tasksByUUID.put(result.component1(), tasksByCode.get(result.component2()));
            uuidByCode.put(result.component2(), result.value1());
        }

        Set<String> codesInDb = results.intoSet(TASK_TYPE.CODE);

        Set<String> codesNotInDb = Sets.difference(tasksByCode.keySet(), codesInDb);

        for (String code : codesNotInDb) {
            UUID uuid = UUID.randomUUID();
            TaskTypeRef taskType = tasksByCode.get(code);
            tasksByUUID.put(uuid, taskType);
            uuidByCode.put(code, uuid);

            ctx
                .insertInto(TASK_TYPE)
                .columns(TASK_TYPE.ID, TASK_TYPE.CODE, TASK_TYPE.NAME)
                .values(uuid, taskType.code(), taskType.name())
                .execute();
        }
    }

    private void createNewQueues(List<TaskTypeRef> refs) {
        for (var ref : refs) {
            if (!ref.isHistoric()) {
                TypeSpecificQueue typeSpecificQueue = applicationContext.getBean(TypeSpecificQueue.class);
                typeSpecificQueue.setType(ref);
                typeSpecificQueue.setThisTaskTypeId(uuidByCode(ref.code()));
                log.info("Registering a TypeSpecificQueue for {}", ref.code());
                typeSpecificQueue.scheduleFirstExecution();
                typeSpecificQueueRegistry.register(ref.code(), typeSpecificQueue);
            }
        }
    }

    public List<TaskTypeRef> all() {
        return new ArrayList<>(tasksByCode.values());
    }

    public List<TaskTypeResponse> allR() {
        return all()
                .stream()
                .map(tt -> new TaskTypeResponse(
                        tt.code(),
                        tt.name(),
                        uuidByCode(tt.code()),
                        tt.isHistoric()
                ))
                .toList();
    }

    public UUID uuidByCode(String code) {
        return uuidByCode.get(code);
    }

    public TaskTypeRef byUUID(UUID uuid) {
        return tasksByUUID.get(uuid);
    }

    public record TaskTypeResponse(String code, String name, UUID id, boolean isHistoric) {

        @Override
        public String toString() {
            return (isHistoric ? "[HISTORIC] " : "") + name + " (" + code + ")";
        }
    }

}
