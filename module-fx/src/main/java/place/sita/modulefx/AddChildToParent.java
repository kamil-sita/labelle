package place.sita.modulefx;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class AddChildToParent {
	public static void addChildToThisPotentialJavaFxParent(Node parent, Node node) {
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
