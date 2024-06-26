package place.sita.magicscheduler.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "place.sita.magic.scheduler.active", havingValue = "true", matchIfMissing = true)
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
