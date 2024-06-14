package place.sita.modulefx;

import javafx.scene.Node;
import place.sita.modulefx.vtg.VirtualTreeGroup;

public interface FxSetupContext {

	Object controller();

	Object parentController();

	Node node();

	Node parentNode();

	FxSceneBuilderProcessors processors();

	@Deprecated
	Node setupForController(Object bean, String resource, FxSetupContext context, VirtualTreeGroup virtualTreeGroup);

	Node setupForController(Object bean, String resource, FxSetupContext context, Object parent, Node parentNode, VirtualTreeGroup virtualTreeGroup);

	VirtualTreeGroup virtualTreeGroup();
}
