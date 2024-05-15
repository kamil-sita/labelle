package place.sita.magicscheduler.scheduler;

public record ExecutionEnvironmentResult(
    boolean exitedWithException,
    boolean isHardFail,
    boolean isDuplicate,
    boolean isSoftFail,
    boolean success) {
}