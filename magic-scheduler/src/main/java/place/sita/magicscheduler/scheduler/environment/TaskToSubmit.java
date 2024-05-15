package place.sita.magicscheduler.scheduler.environment;

import java.util.UUID;

public record TaskToSubmit(
	UUID id,
	String code,
	String parameter) {
}
