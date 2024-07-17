package place.sita.magicscheduler.tasktype.annotationdef;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.tasktype.TaskType;
import place.sita.magicscheduler.tasktype.TaskTypeRegistry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class AnnotationDefBeanPostProcessor implements BeanPostProcessor {

	private final TaskTypeRegistry taskTypeRegistry;

	public AnnotationDefBeanPostProcessor(TaskTypeRegistry taskTypeRegistry) {
		this.taskTypeRegistry = taskTypeRegistry;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class clazz = bean.getClass();
		Arrays.stream(clazz.getMethods()).forEach(method -> {
			if (method.isAnnotationPresent(MsTask.class)) {
				doRegister(method, bean);
			}
		});
		return bean;
	}

	private void doRegister(Method method, Object bean) {
		TaskType<?, ?, ?> tt = AnnotationBasedTaskTypeFactory.build(method, bean);
		taskTypeRegistry.register(List.of(tt));
	}
}
