package place.sita.labelle.gui.local.fx.modulefx;

import javafx.scene.Node;

public interface FxSetupContext {

	Object controller();

	Object parentController();

	Node node();

	Node parentNode();

	FxSceneBuilderProcessors processors();

	Node setupForController(Object bean, Object parentController, Node parentNode, String resource, FxSetupContext context);
}
