package place.sita.labelle.core.tasks.scheduler;

public record ExecutionEnvironmentResult(
    boolean exitedWithException,
    boolean isHardFail,
    boolean isDuplicate,
    boolean isSoftFail,
    boolean success) {
}