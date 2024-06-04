package place.sita.labelle.actions;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;

public class RepositoryActions {

	private RepositoryActions() {

	}

	public static ListView getRepositoryList() {
		return FxAssert.assertContext().getNodeFinder().lookup("#repositoryList").query();
	}

	public static void createNewRepository(FxRobot robot, String name) {

		TextField testField = FxAssert.assertContext().getNodeFinder()
			.lookup("#repositoryNameTextField").query();

		robot.clickOn(testField);
		robot.write("My test repository");
		robot.clickOn("#addNewButton");
	}

}
