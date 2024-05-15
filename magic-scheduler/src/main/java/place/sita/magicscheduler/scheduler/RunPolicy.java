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
        return new RunPolicy() {
            @Override
            public boolean shouldRun(ExecutionEnvironmentResult executionEnvironmentResult) {
                return true;
            }
        };
    }

    static RunPolicy ifJobSucceeded() {
        return new RunPolicy() {
            @Override
            public boolean shouldRun(ExecutionEnvironmentResult executionEnvironmentResult) {
                return executionEnvironmentResult.success();
            }
        };
    }

}
