package place.sita.labelle.actions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;

public class SchedulerExecutorActions {

	private SchedulerExecutorActions() {

	}

	public static void filterBy(FxRobot robot, String filter) {
		TextField textField = FxAssert.assertContext().getNodeFinder()
			.lookup("#filterTextField").query();

		robot.clickOn(textField);
		robot.write(filter);
	}

	public static ListView taskList() {
		return taskListLookup().query();
	}

	public static NodeQuery taskListLookup() {
		return FxAssert.assertContext().getNodeFinder().lookup("#schedulableTaskList");
	}

	public static TextArea codeArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#codeArea").query();
	}

	public static Button executeButton() {
		return FxAssert.assertContext().getNodeFinder().lookup("#executeButton").query();
	}

	public static Button clearCodeArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#clearButton").query();
	}

	public static Node schedulerExecutorTask(int idx) {
		return (Node) taskListLookup().lookup(".list-cell").nth(idx).query();
	}
}
