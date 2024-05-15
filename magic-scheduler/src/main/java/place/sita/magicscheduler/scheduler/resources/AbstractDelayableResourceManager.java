package place.sita.magicscheduler.scheduler.resources;

import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public abstract class AbstractDelayableResourceManager implements DelayableApi, ResourceManager {

	public abstract Duration defaultDelay();

	private Instant nextTicket = null;
	private Duration currentDelay = defaultDelay();
	private Duration currentTimeout = null;

	@Override
	public Duration getCurrentDelay() {
		return currentDelay;
	}

	@Override
	public void increaseDelay() {
		currentDelay = currentDelay.plus(2, ChronoUnit.SECONDS);
	}

	@Override
	public void decreaseDelay() {
		if (currentDelay.compareTo(Duration.of(1, ChronoUnit.SECONDS)) > 0) {
			return;
		}
		currentDelay = currentDelay.minus(1, ChronoUnit.SECONDS);
	}

	@Override
	public void setDelay(Duration duration) {
		currentDelay = duration;
	}

	@Override
	public void timeout(Duration duration) {
		currentTimeout = duration;
	}

	@Override
	public final LockResult tryLock(UUID lockId, Resource resource) {
		return lockInternal(true);
	}

	@Override
	public final LockResult canBeLocked(Resource resource) {
		return lockInternal(false);
	}

	private LockResult lockInternal(boolean actual) {
		if (nextTicket == null) {
			return doTryLock(actual);
		}
		if (Instant.now().isAfter(nextTicket)) {
			return doTryLock(actual);
		} else {
			return new LockResult.LockFailRetryLaterAt(nextTicket);
		}
	}

	protected abstract LockResult doTryLock(boolean actual);

	@Override
	public final void unlock(UUID lockId) {
		if (currentTimeout != null) {
			nextTicket = Instant.now().plus(currentTimeout);
			currentTimeout = null;
		} else {
			nextTicket = Instant.now().plus(currentDelay);
		}
		doUnlock();
	}

	protected abstract void doUnlock();
}
