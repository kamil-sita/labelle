package place.sita.magicscheduler.scheduler.resources;

import java.time.Instant;

public record ResourceState(String discriminator, int taken, Instant nextSuggestedAskForAvailabilityTime) {
}
