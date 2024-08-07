package place.sita.magicscheduler.tasktype;

import com.fasterxml.jackson.core.JsonProcessingException;

import place.sita.magicscheduler.MappingSupport;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.ApiTaskExecutionResult;
import place.sita.magicscheduler.scheduler.FailResolver;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;

public non-sealed interface TaskType<ParameterT, AcceptedContextT, ResultT> extends TaskTypeRef {

    TaskResult<ResultT> runTask(ParameterT parameter, TaskContext<AcceptedContextT> taskContext);

    default ParameterT deserializeParam(String s) {
	    try {
		    return MappingSupport.objectMapper.readValue(s, paramType());
	    } catch (JsonProcessingException e) {
		    throw new RuntimeException(e);
	    }
    }

    default String serializeParam(ParameterT parameterT) {
	    try {
		    return MappingSupport.objectMapper.writeValueAsString(parameterT);
	    } catch (JsonProcessingException e) {
		    throw new RuntimeException(e);
	    }
    }

    default ResultT deserializeResult(String s) {
	    try {
		    return MappingSupport.objectMapper.readValue(s, resultType());
	    } catch (JsonProcessingException e) {
		    throw new RuntimeException(e);
	    }
    }

    default String serializeResult(ResultT result) {
	    try {
		    return MappingSupport.objectMapper.writeValueAsString(result);
	    } catch (JsonProcessingException e) {
		    throw new RuntimeException(e);
	    }
    }

    String sampleValue();

    List<Resource<?>> resources(ParameterT parameterT);

    Class<AcceptedContextT> contextType();

    default ApiTaskExecutionResult resolveThrowableIntoResult(Throwable exception) {
        return FailResolver.resolve(exception);
    }

    Class<ParameterT> paramType();

	Class<ResultT> resultType();

	@Override
	default boolean isHistoric() {
		return false;
	}
}
