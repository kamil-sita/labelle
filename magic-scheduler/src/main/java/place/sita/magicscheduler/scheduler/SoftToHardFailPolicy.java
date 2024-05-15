package place.sita.magicscheduler.scheduler;

public interface SoftToHardFailPolicy {
	boolean shouldFailHard(int failedExecutions);
}
