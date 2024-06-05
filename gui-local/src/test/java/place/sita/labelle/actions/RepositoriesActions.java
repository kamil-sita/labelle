package place.sita.labelle.actions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;

public class RepositoriesActions {

	private RepositoriesActions() {

	}

	public static ListView getRepositoryList() {
		return repositoryListLookup().query();
	}

	private static NodeQuery repositoryListLookup() {
		return FxAssert.assertContext().getNodeFinder().lookup("#repositoryList");
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

	public static Button deleteRepositoryButton() {
		return FxAssert.assertContext().getNodeFinder().lookup("#deleteRepositoryButton").query();
	}

	public static Node repository(int i) {
		return (Node) repositoryListLookup().lookup(".list-cell").nth(i).query();
	}
}
