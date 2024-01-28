package place.sita.labelle.core.tasks.scheduler;

public interface SoftToHardFailPolicy {
	boolean shouldFailHard(int failedExecutions);
}
