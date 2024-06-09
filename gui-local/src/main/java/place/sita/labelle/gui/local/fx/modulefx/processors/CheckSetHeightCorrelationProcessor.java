package place.sita.labelle.gui.local.fx.modulefx.processors;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import place.sita.labelle.gui.local.fx.modulefx.FxSceneBuilderProcessor;
import place.sita.labelle.gui.local.fx.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.FxDictatesHeight;

public class CheckSetHeightCorrelationProcessor implements FxSceneBuilderProcessor {
	@Override
	public void process(FxSetupContext context) {
		Class<?> clazz = context.controller().getClass();
		Node childNode = context.node();
		Node parentNode = context.parentNode();
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
}
