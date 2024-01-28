package place.sita.labelle.core.tasks.scheduler;

public class FailResolver {

    public static ApiTaskExecutionResult resolve(Throwable exception) {
        // todo improve this handler
        if (exception instanceof Error) {
            return ApiTaskExecutionResult.SOFT_FAIL;
        }
        return ApiTaskExecutionResult.HARD_FAIL;
    }

}
