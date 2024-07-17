package place.sita.magicscheduler.tasktype.annotationdef;

import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;
import place.sita.magicscheduler.tasktype.TaskType;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class AnnotationBasedTaskTypeFactory {

	public static TaskType<?, ?, ?> build(Method method, Object object) {
		MsTask msTask = method.getAnnotation(MsTask.class);
		Annotation[][] annotations = method.getParameterAnnotations();
		Integer idxOfParameter = idxOf(annotations, MsParameter.class);
		Integer idxOfSandbox = idxOf(annotations, MsSandbox.class);
		Class parameterType = Void.class;
		if (idxOfParameter != null) {
			Parameter parameter = method.getParameters()[idxOfParameter];
			parameterType = parameter.getType();
		}
		Class finalParameterType = parameterType;
		TaskType tt = new TaskType() {
			@Override
			public TaskResult runTask(Object parameter, TaskContext taskContext) {
				Object[] args = new Object[annotations.length];
				if (idxOfParameter != null) {
					args[idxOfParameter] = parameter;
				}
				if (idxOfSandbox != null) {
					args[idxOfSandbox] = taskContext;
				}
				try {
					method.invoke(object, args);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				return TaskResult.success();
			}

			@Override
			public String sampleValue() {
				return "Not yet implemented";
			}

			@Override
			public List<Resource<?>> resources(Object o) {
				return List.of();
			}

			@Override
			public Class contextType() {
				return Void.class;
			}

			@Override
			public Class paramType() {
				return finalParameterType;
			}

			@Override
			public Class resultType() {
				return Void.class;
			}

			@Override
			public String code() {
				return msTask.code();
			}

			@Override
			public String name() {
				return msTask.name();
			}
		};

		return tt;
	}

	private static Integer idxOf(Annotation[][] annotations, Class<? extends Annotation> annotationClass) {
		for (int i = 0; i < annotations.length; i++) {
			for (Annotation annotation : annotations[i]) {
				if (annotation.annotationType().equals(annotationClass)) {
					return i;
				}
			}
		}
		return null;
	}

}
