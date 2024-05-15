package place.sita.magicscheduler.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "place.sita.magic.scheduler")
public class SchedulerProperties {

	private boolean queueEnabled = true;

	public boolean isQueueEnabled() {
		return queueEnabled;
	}

	public void setQueueEnabled(boolean queueEnabled) {
		this.queueEnabled = queueEnabled;
	}
}
