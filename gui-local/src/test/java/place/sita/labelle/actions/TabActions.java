package place.sita.labelle.actions;

import javafx.scene.Node;
import org.testfx.api.FxAssert;

public class TabActions {

	private TabActions() {

	}

	public static Node getTabNode(String pane, String tabName) {
		return FxAssert.assertContext().getNodeFinder()
			.lookup(pane)
			.lookup(".tab-header-area > .headers-region > .tab")
			.lookup(tabName).query();
	}

	public static Node getMainTab(String tabName) {
		return getTabNode("#mainTabPane", tabName);
	}

}
