package place.sita.magicscheduler.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import place.sita.magicscheduler.InternalTaskSubmitter;
import place.sita.magicscheduler.TestContainersTest;
import place.sita.magicscheduler.scheduler.task.TestTask;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MagicSchedulerTest extends TestContainersTest {

	@Autowired
	private MagicScheduler magicScheduler;

	@Autowired
	private TestTask testTask;

	@MockBean
	private MagicSchedulerBackend magicSchedulerBackend;

	@MockBean
	private ScheduleLater scheduleLater;

	@Autowired
	private InternalTaskSubmitter internalTaskSubmitter;

	@Captor
	private ArgumentCaptor<Runnable> argumentCaptor;

	@Test
	public void shouldRunTask() {
		// given
		MagicScheduler.ExecutionFinishedCallback callback = Mockito.mock(MagicScheduler.ExecutionFinishedCallback.class);

		// when
		UUID id = internalTaskSubmitter.submitTaskForLater(testTask.code(), "test", InternalTaskSubmitter.UUID_FOR_USER_SUBMITTED_TASKS);
		magicScheduler.schedule(id, testTask, "test", 0, callback);

		// then
		verify(callback, never()).onExecutionFinished();

		// when
		verify(magicSchedulerBackend).runLater(argumentCaptor.capture());
		argumentCaptor.getValue().run();

		// then
		verify(callback).onExecutionFinished();
	}

}
