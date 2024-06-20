package place.sita.magicscheduler.scheduler;

@FunctionalInterface
public interface RunPolicy {

    boolean shouldRun(ExecutionEnvironmentResult executionEnvironmentResult);

    default RunPolicy or(RunPolicy other) {
        return or(this, other);
    }

    default RunPolicy and(RunPolicy other) {
        return and(this, other);
    }

    static RunPolicy or(RunPolicy first, RunPolicy second) {
        return eer -> first.shouldRun(eer) || second.shouldRun(eer);
    }

    static RunPolicy and(RunPolicy first, RunPolicy second) {
        return eer -> first.shouldRun(eer) && second.shouldRun(eer);
    }

    static RunPolicy always() {
        return executionEnvironmentResult -> true;
    }

    static RunPolicy ifJobSucceeded() {
        return executionEnvironmentResult -> executionEnvironmentResult.success();
    }

}
