package place.sita.labelle.gui.local.fx.modulefx;

import javafx.scene.Node;

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
			public Node setupForController(Object bean, String resource, FxSetupContext context) {
				return internalSetupForController(bean, controller, results, resource, context.processors());
			}

			@Override
			public Node setupForController(Object bean, String resource, FxSetupContext context, Object parent, Node parentNode) {
				return internalSetupForController(bean, parent, parentNode, resource, context.processors());
			}
		});

	    return results;
	}
}
