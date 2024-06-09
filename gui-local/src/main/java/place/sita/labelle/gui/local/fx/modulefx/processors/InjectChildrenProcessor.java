package place.sita.labelle.gui.local.fx.modulefx.processors;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.util.ReflectionUtils;
import place.sita.labelle.gui.local.fx.modulefx.ChildrenFactory;
import place.sita.labelle.gui.local.fx.modulefx.FxSceneBuilderProcessor;
import place.sita.labelle.gui.local.fx.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxNode;

import java.lang.reflect.Field;
import java.util.Arrays;

public class InjectChildrenProcessor implements FxSceneBuilderProcessor {

	private final ChildrenFactory childrenFactory;

	public InjectChildrenProcessor(ChildrenFactory childrenFactory) {
		this.childrenFactory = childrenFactory;
	}

	@Override
	public void process(FxSetupContext context) {
		Object controller = context.controller();

		Class<?> controllerClass = controller.getClass();
		Arrays.stream(controllerClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(FxChild.class))
			.forEach(field -> {
				injectChild(controller, field, controllerClass, context);
			});
	}

	private void injectChild(Object parentController, Field fieldWithAnnotation, Class<?> parentControllerClass, FxSetupContext context) {
		FxChild childConfig = fieldWithAnnotation.getAnnotation(FxChild.class);
		try {
			Class<?> classType = fieldWithAnnotation.getType();
			if (classType.isAnnotationPresent(FxNode.class)) {
				Node parentNode = getParentNode(parentController, parentControllerClass, childConfig, classType);

				String resource = classType.getAnnotation(FxNode.class).resourceFile();
				Object bean = childrenFactory.create(classType);
				Node node = context.setupForController(bean, parentController, parentNode, resource, context);
				fieldWithAnnotation.setAccessible(true);
				fieldWithAnnotation.set(parentController, bean);

				// patch correlated node in parent controller
				addChildToThisPotentialJavaFxParent(parentNode, node);
			} else {
				throw new RuntimeException("Cannot inject something that is not an @FxNode");
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Node getParentNode(Object parentController, Class<?> parentControllerClass, FxChild childConfig, Class<?> classType) throws IllegalAccessException {
		// todo validate whether we keep on patching the same node; if it's not something that explicitly
		//  should hold many children, we should throw an exception

		String fieldToPatchName = childConfig.patchNode();
		Field fieldToPatch = ReflectionUtils.findField(parentControllerClass, fieldToPatchName);
		if (fieldToPatch == null) {
			throw new RuntimeException("Cannot find field to patch: " + fieldToPatchName + " in " + classType.getName());
		}
		fieldToPatch.setAccessible(true);
		Object potentialJavaFxParent = fieldToPatch.get(parentController);
		if (potentialJavaFxParent instanceof Node node) {
			return node;
		}
		throw new RuntimeException("Cannot patch field: " + fieldToPatchName + " in " + classType.getName() + ": not a Node");
	}

	private static void addChildToThisPotentialJavaFxParent(Node parent, Node node) {
		if (parent instanceof Pane pane) {
			pane.getChildren().setAll(node);
			AnchorPane.setTopAnchor(node, 0.0);
			AnchorPane.setLeftAnchor(node, 0.0);
			AnchorPane.setRightAnchor(node, 0.0);
			AnchorPane.setBottomAnchor(node, 0.0);
		} else {
			throw new RuntimeException("Not a Pane - cannot inject");
		}
	}

}
