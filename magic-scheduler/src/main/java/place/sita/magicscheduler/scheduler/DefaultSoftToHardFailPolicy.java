package place.sita.magicscheduler.scheduler;

import org.springframework.stereotype.Component;

@Component
public class DefaultSoftToHardFailPolicy implements SoftToHardFailPolicy {


    @Override
    public boolean shouldFailHard(int failedExecutions) {
        return failedExecutions >= 3;
    }
}
