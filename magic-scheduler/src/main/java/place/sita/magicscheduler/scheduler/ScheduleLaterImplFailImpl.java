package place.sita.magicscheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "place.sita.magic.scheduler.active", havingValue = "false")
public class ScheduleLaterImplFailImpl implements ScheduleLater {

	private static final Logger log = LoggerFactory.getLogger(ScheduleLaterImplFailImpl.class);

	public ScheduleLaterImplFailImpl() {
		log.warn("No TaskScheduler implementation found. Tasks will not be scheduled.");
	}

	@Override
	public void schedule(Runnable task, Instant startTime) {
		throw new IllegalStateException("No TaskScheduler implementation found. Tasks will not be scheduled.");
	}
}
