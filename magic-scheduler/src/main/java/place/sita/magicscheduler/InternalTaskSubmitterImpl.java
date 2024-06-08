package place.sita.magicscheduler;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.scheduler.TypeSpecificQueueRegistry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.*;

@Component
public class InternalTaskSubmitterImpl implements InternalTaskSubmitter {

	private final DSLContext dslContext;
    private final TypeSpecificQueueRegistry registry;

    public InternalTaskSubmitterImpl(DSLContext dslContext,
                                     TypeSpecificQueueRegistry registry) {
        this.dslContext = dslContext;
	    this.registry = registry;
    }

    @Override
    @Transactional
    public void submitTaskForLater(UUID id, String code, String parameter, UUID parent, List<UUID> requirements) {
        UUID taskTypeId = JqRepo.fetchOne(() ->
            dslContext
                .select(TASK_TYPE.ID)
                .from(TASK_TYPE)
                .where(TASK_TYPE.CODE.eq(code))
                .fetch()
        );

        JqRepo.insertOne(() ->
            dslContext.insertInto(TASK)
                .columns(TASK.ID, TASK.TASK_TYPE_ID, TASK.CREATION_DATE, TASK.STATUS, TASK.PARENT)
                .values(id, taskTypeId, LocalDateTime.now(), TaskStatus.CREATED, parent)
                .execute()
        );

        JqRepo.insertOne(() ->
            dslContext.insertInto(TASK_CONFIG)
                .columns(TASK_CONFIG.TASK_ID, TASK_CONFIG.CONFIG)
                .values(id, parameter)
                .execute()
        );

        for (UUID requirement : requirements) {
            JqRepo.insertOne(() ->
                dslContext.insertInto(TASK_DEPENDENCIES)
                        .columns(TASK_DEPENDENCIES.TASK_ID, TASK_DEPENDENCIES.REQUIRED_DEPENDENCY_TASK_ID)
                        .values(id, requirement)
                        .execute()
            );
        }

        if (UUID_FOR_USER_SUBMITTED_TASKS.equals(parent)) {
            registry.get(code).scheduleWithoutQueue(id, parameter);
        }
    }
}
