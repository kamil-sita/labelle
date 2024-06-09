package place.sita.labelle.gui.local.fx.modulefx;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
}
