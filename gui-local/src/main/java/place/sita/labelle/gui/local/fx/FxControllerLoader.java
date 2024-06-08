package place.sita.labelle.gui.local.fx;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import place.sita.modulefx.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.UUID;

@Component
public class FxControllerLoader {

	private final ApplicationContext context;
	private final UnstableSceneReporter unstableSceneReporter;

	public FxControllerLoader(ApplicationContext context, UnstableSceneReporter unstableSceneReporter) {
		this.context = context;
		this.unstableSceneReporter = unstableSceneReporter;
	}

	public Node setupForController(Object controller, String resource) {
		UUID loadId = UUID.randomUUID();
		try {
			unstableSceneReporter.markUnstable(loadId, "Loading of controller: " + controller.getClass().getName());
			return setupForController(controller, null, null, resource);
		} finally {
			unstableSceneReporter.markStable(loadId);
		}
	}

	private Node setupForController(Object controller, Object parent, Node parentNode, String resource) {
	    Node results = FxSceneBuilder.setupFxView(controller, resource);

		setupParent(controller, parent, parentNode, results);
		injectChildren(controller);
	    callPostFxInject(controller);

	    return results;
	}

	private void setupParent(Object controller, Object parent, Node parentNode, Node childNode) {
		Class<?> clazz = controller.getClass();

		checkSetHeightCorrelation(parentNode, childNode, clazz);

		injectParentField(controller, parent, clazz);
	}

	private static void checkSetHeightCorrelation(Node parentNode, Node childNode, Class<?> clazz) {
		if (clazz.isAnnotationPresent(FxDictatesHeight.class)) {
			if (parentNode instanceof Region parentAsRegion) {
				if (childNode instanceof Region childAsRegion) {
					// todo this probably should not happen if it's a region that hold multiple children

					parentAsRegion.setMaxHeight(childAsRegion.getMaxHeight());
					parentAsRegion.setMaxWidth(childAsRegion.getMaxWidth());

					parentAsRegion.setMinHeight(childAsRegion.getMinHeight());
					parentAsRegion.setMinWidth(childAsRegion.getMinWidth());

					parentAsRegion.setPrefHeight(childAsRegion.getPrefHeight());
					parentAsRegion.setPrefWidth(childAsRegion.getPrefWidth());
				} else {
					throw new RuntimeException(
							"Non-region cannot dictate height. Node of " + childNode.getClass().getName() + " is not a Region");
				}
			} else {
				throw new RuntimeException(
						"Cannot dictate height of non-Region: Node of " + parentNode.getClass().getName() + " is not a Region");
			}
		}
	}

	private static void injectParentField(Object controller, Object parent, Class<?> clazz) {
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
