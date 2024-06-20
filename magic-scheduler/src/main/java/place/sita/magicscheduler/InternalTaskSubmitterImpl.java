package place.sita.magicscheduler;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.scheduler.TypeSpecificQueue;
import place.sita.magicscheduler.scheduler.TypeSpecificQueueRegistry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.*;

@Component
public class InternalTaskSubmitterImpl implements InternalTaskSubmitter {

    private final TypeSpecificQueueRegistry registry;
    private final DatabaseSubmitter databaseSubmitter;

    public InternalTaskSubmitterImpl(TypeSpecificQueueRegistry registry,
                                     DatabaseSubmitter databaseSubmitter) {
	    this.registry = registry;
	    this.databaseSubmitter = databaseSubmitter;
    }

    @Override
    public void submitTaskForLater(UUID id, String code, String parameter, UUID parent, List<UUID> requirements) {
        TypeSpecificQueue queue = registry.get(code);

        scheduleInternal(id, code, parameter, parent, requirements, queue);
    }

    /**
     * This is a somewhat hacky implementation - generally, it does not matter if a job is scheduled now or in a few
     * seconds. However, users, and tests like scheduling to be snappy - and by scheduling directly,
     * it is very fast! <br/>
     *
     * However, there's a small problem down there in the implementation. First of all, this method is
     * used to schedule tasks both from user and from the system. We do not need to prioritize the system ones,
     * so we don't. <br/>
     *
     * We also do not want skipping of queue to happen in a transaction - what would happen if the transaction
     * is rolled back? The task might have execution started, but it would not be registered in the system.
     */
    private void scheduleInternal(UUID id, String code, String parameter, UUID parent, List<UUID> requirements, TypeSpecificQueue queue) {
        queue.doInSchedulingLock(context -> {
            databaseSubmitter.submitTaskForLater(id, code, parameter, parent, requirements);

            boolean userSubmitted = !UUID_FOR_USER_SUBMITTED_TASKS.equals(parent);
            boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();

            if (userSubmitted && !inTransaction) {
                context.scheduleWithoutQueue(id, parameter);
            }
        });
    }

    @Component
    public static class DatabaseSubmitter {

        private final DSLContext dslContext;

	    public DatabaseSubmitter(DSLContext dslContext) {
		    this.dslContext = dslContext;
	    }

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
        }
    }
}
