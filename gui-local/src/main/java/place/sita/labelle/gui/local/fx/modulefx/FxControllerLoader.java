package place.sita.labelle.gui.local.fx.modulefx;

import javafx.scene.Node;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.UnstableSceneReporter;

import java.util.UUID;

public class FxControllerLoader {

	public static Node setupForController(Object controller, String resource, FxSceneBuilderProcessors processors) {
		return internalSetupForController(controller, null, null, resource, processors);
	}

	private static Node internalSetupForController(Object controller, Object parent, Node parentNode, String resource, FxSceneBuilderProcessors fxSceneBuilderProcessors) {
	    Node results = FxSceneBuilder.loadNodeForController(controller, resource);

		fxSceneBuilderProcessors.runAll(new FxSetupContext() {
			@Override
			public Object controller() {
				return controller;
			}

			@Override
			public Object parentController() {
				return parent;
			}

			@Override
			public Node node() {
				return results;
			}

			@Override
			public Node parentNode() {
				return parentNode;
			}

			@Override
			public FxSceneBuilderProcessors processors() {
				return fxSceneBuilderProcessors;
			}

			@Override
			public Node setupForController(Object bean, Object parentController, Node parentNode, String resource, FxSetupContext context) {
				return internalSetupForController(bean, parentController, parentNode, resource, context.processors());
			}
		});

	    return results;
	}
}
