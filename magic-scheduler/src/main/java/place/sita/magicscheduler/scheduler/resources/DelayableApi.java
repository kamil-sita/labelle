package place.sita.magicscheduler.scheduler.resources;

import java.time.Duration;

public interface DelayableApi {

    Duration getCurrentDelay();

    void increaseDelay();

    void decreaseDelay();

    void setDelay(Duration duration);

    void timeout(Duration duration);

}
