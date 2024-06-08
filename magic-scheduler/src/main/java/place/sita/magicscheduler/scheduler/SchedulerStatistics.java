package place.sita.magicscheduler.scheduler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.scheduler.events.TaskExecutionCompleteEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SchedulerStatistics {

	private final AtomicInteger successfulTaskCount = new AtomicInteger(0);

	@EventListener
	public void onTaskResult(TaskExecutionCompleteEvent event) {
		if (event.success()) {
			successfulTaskCount.incrementAndGet();
		}
	}

	public int successfulTaskCount() {
		return successfulTaskCount.get();
	}

}
