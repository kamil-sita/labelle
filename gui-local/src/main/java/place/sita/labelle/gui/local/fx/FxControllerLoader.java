package place.sita.labelle.gui.local.fx;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.Parent;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Component
public class FxControllerLoader {

	private final ApplicationContext context;

	public FxControllerLoader(ApplicationContext context) {
		this.context = context;
	}

	public Node setupForController(Object controller, String resource) {
		return setupForController(controller, null, null, resource);
	}

	private Node setupForController(Object controller, Object parent, Node parentNode, String resource) {
	    Node results = FxSceneBuilder.setupFxView(controller, resource);

		setupParent(controller, parent, parentNode);
		injectChildren(controller);
	    callPostFxInject(controller);

	    return results;
	}

	private void setupParent(Object controller, Object parent, Node parentNode) {
		Class<?> clazz = controller.getClass();
		Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(Parent.class))
			.forEach(field -> {
				try {
					if (parent == null) {
						throw new RuntimeException("Cannot inject a null parent");
					}
					field.setAccessible(true);
					field.set(controller, parent);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
	}

	private void injectChildren(Object controller) {
		Class<?> controllerClass = controller.getClass();
		Arrays.stream(controllerClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(FxChild.class))
			.forEach(field -> {
				injectChild(controller, field, controllerClass);
			});

	}

	private void injectChild(Object parentController, Field fieldWithAnnotation, Class<?> parentControllerClass) {
		FxChild childConfig = fieldWithAnnotation.getAnnotation(FxChild.class);
		try {
			Class<?> classType = fieldWithAnnotation.getType();
			if (classType.isAnnotationPresent(FxNode.class)) {
				Node parentNode = getParentNode(parentController, parentControllerClass, childConfig, classType);

				String resource = classType.getAnnotation(FxNode.class).resourceFile();
				Object bean = context.getBean(classType);
				Node node = setupForController(bean, parentController, parentNode, resource);
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

	private static void callPostFxInject(Object controller) {
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
