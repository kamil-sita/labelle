package place.sita.magicscheduler.scheduler;

import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.magicscheduler.*;
import place.sita.magicscheduler.scheduler.task.StringTaskTestValueProcessor;
import place.sita.magicscheduler.scheduler.task.StringTestTask;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MagicSchedulerTest extends TestContainersTest {

	@Autowired
	private MagicScheduler magicScheduler;

	@Autowired
	private StringTestTask stringTestTask;

	@MockBean
	private MagicSchedulerBackend magicSchedulerBackend;

	@MockBean
	private ScheduleLater scheduleLater;

	@MockBean
	private StringTaskTestValueProcessor stringTaskTestValueProcessor;

	@Autowired
	private InternalTaskSubmitter internalTaskSubmitter;

	@Captor
	private ArgumentCaptor<Runnable> argumentCaptor;

	@Autowired
	private ExecutionsService executionsService;

	@Test
	public void shouldRunTask() {
		// given
		MagicScheduler.ExecutionFinishedCallback callback = Mockito.mock(MagicScheduler.ExecutionFinishedCallback.class);
		when(stringTaskTestValueProcessor.process(any(), any())).thenReturn(TaskResult.success("Success: test"));

		// when
		runTasksAsTest(stringTestTask, "test", 0, callback);

		// then
		verify(callback, never()).onExecutionFinished();
		var tasksBefore = executionsService.getScheduledTasks(Integer.MAX_VALUE, 0, null);
		assertThat(tasksBefore).hasSize(1);
		assertThat(tasksBefore.get(0).taskCode()).isEqualTo(stringTestTask.code());
		assertThat(tasksBefore.get(0).taskName()).isEqualTo(stringTestTask.name());
		assertThat(tasksBefore.get(0).jobCreationTime()).isCloseTo(LocalDateTime.now(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(tasksBefore.get(0).taskStatus()).isEqualTo(TaskStatus.CREATED);
		var executionsBefore = executionsService.getExecutions(tasksBefore.get(0).taskId());
		assertThat(executionsBefore).isEmpty();

		// when
		verify(magicSchedulerBackend).runLater(argumentCaptor.capture());
		argumentCaptor.getValue().run();

		// then
		verify(callback).onExecutionFinished();
		var tasksAfter = executionsService.getScheduledTasks(Integer.MAX_VALUE, 0, null);
		assertThat(tasksAfter).hasSize(1);
		assertThat(tasksAfter.get(0).taskCode()).isEqualTo(stringTestTask.code());
		assertThat(tasksAfter.get(0).taskName()).isEqualTo(stringTestTask.name());
		assertThat(tasksAfter.get(0).jobCreationTime()).isCloseTo(LocalDateTime.now(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(tasksAfter.get(0).taskStatus()).isEqualTo(TaskStatus.DONE);
		var executionsAfter = executionsService.getExecutions(tasksBefore.get(0).taskId());
		assertThat(executionsAfter).hasSize(1);
		assertThat(executionsAfter.get(0).result()).isEqualTo(TaskExecutionResult.DONE);
		assertThat(executionsAfter.get(0).log()).isEmpty();
		assertThat(executionsAfter.get(0).startedAt()).isCloseTo(OffsetDateTime.now(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(executionsAfter.get(0).finishedAt()).isCloseTo(OffsetDateTime.now(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(executionsAfter.get(0).configuration()).isEqualTo("\"test\"");
		// todo test return value
	}

	private <ParameterT> void runTasksAsTest(TaskType<ParameterT, ?, ?> taskType,
	                                         ParameterT parameter,
	                                         int executionCount,
	                                         MagicScheduler.ExecutionFinishedCallback callback) {
		UUID id;
		try {
			id = internalTaskSubmitter.submitTaskForLater(taskType.code(), new ObjectMapper().writeValueAsString(parameter), InternalTaskSubmitter.UUID_FOR_USER_SUBMITTED_TASKS);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		magicScheduler.schedule(id, taskType, parameter, executionCount, callback);
	}

}
