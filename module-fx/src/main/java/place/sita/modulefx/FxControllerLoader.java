package place.sita.modulefx;

import javafx.scene.Node;
import place.sita.modulefx.vtg.VirtualTreeGroup;

public class FxControllerLoader {

	public static Node setupForController(Object controller, String resource, FxSceneBuilderProcessors processors, VirtualTreeGroup virtualTreeGroup) {
		return internalSetupForController(controller, null, null, resource, processors, virtualTreeGroup);
	}


	private static Node internalSetupForController(Object controller, Object parent, Node parentNode, String resource, FxSceneBuilderProcessors fxSceneBuilderProcessors, VirtualTreeGroup virtualTreeGroup) {
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
			public Node setupForController(Object bean, String resource, FxSetupContext context, VirtualTreeGroup virtualTreeGroup) {
				return internalSetupForController(bean, controller, results, resource, context.processors(), virtualTreeGroup);
			}

			@Override
			public Node setupForController(Object bean, String resource, FxSetupContext context, Object parent, Node parentNode, VirtualTreeGroup virtualTreeGroup) {
				return internalSetupForController(bean, parent, parentNode, resource, context.processors(), virtualTreeGroup);
			}

			@Override
			public VirtualTreeGroup virtualTreeGroup() {
				return virtualTreeGroup;
			}
		});

	    return results;
	}
}
