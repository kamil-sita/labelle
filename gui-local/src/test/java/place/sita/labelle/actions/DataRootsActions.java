package place.sita.labelle.actions;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;

public class DataRootsActions {

	private DataRootsActions() {

	}

	public static ListView getRootList() {
		return FxAssert.assertContext().getNodeFinder().lookup("#rootListView").query();
	}

	public static void createDataRoot(FxRobot robot, String path) {
		TextField textField = FxAssert.assertContext().getNodeFinder()
			.lookup("#textField").query();

		robot.clickOn(textField);
		robot.write(path);
		robot.clickOn("#addRootButton");
	}
}
