package place.sita.modulefx.processors;

import place.sita.modulefx.FxSceneBuilderProcessor;
import place.sita.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.vtg.VirtualTreeGroupElement;

import java.util.Arrays;

public class MessageBusSupportProcessor implements FxSceneBuilderProcessor {
	@Override
	public void process(FxSetupContext context) {
		VirtualTreeGroupElement element = new VirtualTreeGroupElement();
		boolean addedToAnyVirtualTreeGroup = false;


		Object controller = context.controller();

		Class<?> controllerClass = controller.getClass();
		Arrays.stream(controllerClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(FxChild.class))
			.forEach(field -> {
				injectChild(controller, field, controllerClass, context);
			});
	}
}
