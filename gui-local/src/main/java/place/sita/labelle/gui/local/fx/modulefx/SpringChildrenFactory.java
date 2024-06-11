package place.sita.labelle.gui.local.fx.modulefx;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpringChildrenFactory implements ChildrenFactory {

	private final ApplicationContext context;

	public SpringChildrenFactory(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public <T> T create(Class<T> clazz) {
		return context.getBean(clazz);
	}

	@Override
	public <T> List<Class<?>> getClasses(Class<T> clazz) {
		ClassPathScanningCandidateComponentProvider provider =
			new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AssignableTypeFilter(clazz));

		List<Class<?>> classes = new ArrayList<>();

		for (BeanDefinition beanDef : provider.findCandidateComponents("place.sita.labelle")) {
			try {
				// let's hope that this is the actual class, and not something else.
				// as it doesn't have to be, see javadoc
				Class<?> beanClass = Class.forName(beanDef.getBeanClassName());
				classes.add(beanClass);
			} catch (ClassNotFoundException e) {
				throw new LoadClassesException(e);
			}
		}

		return classes;
	}

	private static class LoadClassesException extends RuntimeException {
		public LoadClassesException(Exception e) {
			super(e);
		}
	}
}
