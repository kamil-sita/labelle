package place.sita.modulefx.processors;

import place.sita.modulefx.FxSceneBuilderProcessor;
import place.sita.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.Parent;

import java.util.Arrays;

public class InjectParentFieldProcessor implements FxSceneBuilderProcessor {
	@Override
	public void process(FxSetupContext context) {
		Object controller = context.controller();
		Class<?> clazz = controller.getClass();
		Object parentController = context.parentController();

		Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Parent.class))
			.forEach(field -> {
				try {
					if (parentController == null) {
						throw new RuntimeException("Cannot inject a null parent");
					}
					field.setAccessible(true);
					field.set(controller, parentController);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
	}
}
