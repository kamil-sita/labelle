package place.sita.magicscheduler.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnBean(TaskScheduler.class)
public class ScheduleLaterImpl implements ScheduleLater {
	private final TaskScheduler taskScheduler;

	public ScheduleLaterImpl(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	@Override
	public void schedule(Runnable task, Instant startTime) {
		taskScheduler.schedule(task, startTime);
	}
}
