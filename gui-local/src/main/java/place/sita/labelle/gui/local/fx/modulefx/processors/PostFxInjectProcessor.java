package place.sita.labelle.gui.local.fx.modulefx.processors;

import place.sita.labelle.gui.local.fx.modulefx.FxSceneBuilderProcessor;
import place.sita.labelle.gui.local.fx.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class PostFxInjectProcessor implements FxSceneBuilderProcessor {
	@Override
	public void process(FxSetupContext context) {
		Object controller = context.controller();
		Class<?> clazz = controller.getClass();
		Arrays.stream(clazz.getMethods())
			.filter(m -> m.isAnnotationPresent(PostFxConstruct.class))
			.forEach(m -> {
				try {
					m.invoke(controller);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
	}
}
