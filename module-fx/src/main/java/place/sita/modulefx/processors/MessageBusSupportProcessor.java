package place.sita.modulefx.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import place.sita.modulefx.FxSceneBuilderProcessor;
import place.sita.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.FxMessageListener;
import place.sita.modulefx.annotations.ModuleFx;
import place.sita.modulefx.messagebus.MessageSender;
import place.sita.modulefx.vtg.MessageListener;
import place.sita.modulefx.vtg.VirtualTreeGroupElement;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageBusSupportProcessor implements FxSceneBuilderProcessor {
	private static final Logger logger = LoggerFactory.getLogger(MessageBusSupportProcessor.class);
	@Override
	public void process(FxSetupContext context) {
		VirtualTreeGroupElement element = new VirtualTreeGroupElement();
		AtomicBoolean addedToAnyVirtualTreeGroup = new AtomicBoolean(false);

		Object controller = context.controller();

		Class<?> controllerClass = controller.getClass();
		Arrays.stream(controllerClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(ModuleFx.class))
			// instance of MessageSender?
			.filter(field -> field.getType().isAssignableFrom(MessageSender.class))
			.forEach(field -> {
				addedToAnyVirtualTreeGroup.set(true);
				UUID elementId = element.getId();
				MessageSender messageSender = new MessageSender() {
					@Override
					public void send(Object message) {
						context.virtualTreeGroup().message(elementId, message);
					}
				};
				field.setAccessible(true);
				try {
					field.set(controller, messageSender);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});

		Arrays.stream(controllerClass.getMethods())
			.filter(method -> method.isAnnotationPresent(FxMessageListener.class))
			.filter(method -> method.getParameterCount() == 1)
			.forEach(method -> {
				addedToAnyVirtualTreeGroup.set(true);
				element.addListener(new MessageListener() {
					@Override
					public void receive(Object message) {
						// check if message instanceof whatever we're listening for
						if (message.getClass().isAssignableFrom(method.getParameterTypes()[0])) {
							try {
								method.invoke(controller, message);
							} catch (Exception e) {
								logger.error("Exception in message listener", e);
							}
						}
					}
				});
			});


		if (addedToAnyVirtualTreeGroup.get()) {
			context.virtualTreeGroup().addElement(element);
		}
	}
}
