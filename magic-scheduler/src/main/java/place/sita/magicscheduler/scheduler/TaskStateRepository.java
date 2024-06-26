package place.sita.magicscheduler.scheduler;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import place.sita.labelle.core.persistence.ex.UnexpectedDatabaseReplyException;
import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.enums.TaskStatus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.TASK;
import static place.sita.labelle.jooq.Tables.TASK_EXECUTION;

@Repository
public class TaskStateRepository {

    private static final Logger log = LoggerFactory.getLogger(TaskStateRepository.class);

    private final DSLContext dslContext;

    public TaskStateRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void assignState(UUID id, TaskStatus status) {
        if (status == null) {
            throw new NullPointerException();
        }

        int u = dslContext
            .update(TASK)
            .set(TASK.STATUS, status)
            .where(TASK.ID.eq(id))
            .execute();
        if (u != 1) {
            boolean exists = dslContext
                .fetchExists(TASK, TASK.ID.eq(id));
            if (exists) {
                TaskStatus currentStatus = dslContext
                    .select(TASK.STATUS)
                    .from(TASK)
                    .where(TASK.ID.eq(id))
                    .fetchOne(TASK.STATUS);
                log.error("Failed to update task status for task {} with status {} to status {}", id, currentStatus, status);
            } else {
                log.error("Failed to update task status for task {} to status {} - it does not exist", id, status);
            }
            throw new UnexpectedDatabaseReplyException();
        }
    }

    public void saveExecution(UUID id, UUID taskId, String log, TaskExecutionResult result, Instant start, Instant end, String results) {
        int u = dslContext
            .insertInto(TASK_EXECUTION)
            .columns(TASK_EXECUTION.ID, TASK_EXECUTION.TASK_ID, TASK_EXECUTION.LOG, TASK_EXECUTION.FINISHED_AT, TASK_EXECUTION.STARTED_AT, TASK_EXECUTION.RESULT, TASK_EXECUTION.RESULT_VALUE)
            .values(id, taskId, log, end.atOffset(ZoneOffset.UTC), start.atOffset(ZoneOffset.UTC), result, results)
            .execute();

        if (u != 1) {
            throw new UnexpectedDatabaseReplyException();
        }
    }

}
