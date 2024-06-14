package place.sita.modulefx;

import javafx.scene.Node;

public interface FxSetupContext {

	Object controller();

	Object parentController();

	Node node();

	Node parentNode();

	FxSceneBuilderProcessors processors();

	@Deprecated
	Node setupForController(Object bean, String resource, FxSetupContext context);

	Node setupForController(Object bean, String resource, FxSetupContext context, Object parent, Node parentNode);
}
