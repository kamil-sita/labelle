package place.sita.magicscheduler.scheduler.resources.resource.builtin;

import place.sita.magicscheduler.scheduler.resources.AbstractDelayableResourceManager;
import place.sita.magicscheduler.scheduler.resources.LockResult;

import java.util.concurrent.Semaphore;

public abstract class AbstractWebResourceManager extends AbstractDelayableResourceManager {

	private final Semaphore lock = new Semaphore(1);

	@Override
	protected LockResult doTryLock(boolean actual) {
		return lockInternal(actual);
	}

	private LockResult lockInternal(boolean holdLock) {
		boolean acquired = lock.tryAcquire();

		if (acquired) {
			if (!holdLock) {
				lock.release();
			}
			return new LockResult.LockedSuccessfully();
		} else {
			return new LockResult.LockFailRetryLater();
		}
	}

	@Override
	protected void doUnlock() {
		lock.release();
	}
}
