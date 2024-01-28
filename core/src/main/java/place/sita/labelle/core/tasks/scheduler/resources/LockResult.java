package place.sita.labelle.core.tasks.scheduler.resources;

import java.time.Instant;

public sealed interface LockResult {

    default boolean isSuccess() {
        return false;
    }

    record LockedSuccessfully() implements LockResult {

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    record LockFailRetryLaterAt(Instant suggestedTime) implements LockResult {

    }

    record LockFailRetryLater() implements LockResult {

    }
}
