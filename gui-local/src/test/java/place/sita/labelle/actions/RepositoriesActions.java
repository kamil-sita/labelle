package place.sita.labelle.actions;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;

public class RepositoriesActions {

	private RepositoriesActions() {

	}

	public static ListView getRepositoryList() {
		return FxAssert.assertContext().getNodeFinder().lookup("#repositoryList").query();
	}

	public static void createNewRepository(FxRobot robot, String name) {

		TextField textField = FxAssert.assertContext().getNodeFinder()
			.lookup("#repositoryNameTextField").query();

		robot.clickOn(textField);
		robot.write(name);
		robot.clickOn("#addNewButton");
	}

	public static TextField uuidField() {
		return FxAssert.assertContext().getNodeFinder().lookup("#uuidField").query();
	}
}
