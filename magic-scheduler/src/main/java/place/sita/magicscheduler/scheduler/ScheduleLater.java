package place.sita.magicscheduler.scheduler;

import java.time.Instant;

public interface ScheduleLater {

	void schedule(Runnable task, Instant startTime);

}
