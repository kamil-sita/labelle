package place.sita.labelle.actions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.testfx.api.FxAssert;
import org.testfx.service.query.NodeQuery;

public class RepositoryActions {

	private RepositoryActions() {

	}

	public static ChoiceBox repositoryChoiceBox() {
		return repositoryChoiceBoxQuery().query();
	}

	public static NodeQuery repositoryChoiceBoxQuery() {
		return FxAssert.assertContext().getNodeFinder().lookup("#repositoryChoiceBox");
	}

	public static Node repositoryChoiceBoxElement(String name) {
		return (Node) RepositoryActions.repositoryChoiceBoxQuery().lookup(".menu-item").lookup(name).query();
	}

	public static Node imageInPaginator(int i) {
		return FxAssert.assertContext().getNodeFinder().lookup("#paginator").lookup(".list-view").lookup(".list-cell").nth(i).query();
	}

	public static TextField familyTextField() {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagFamilyTextField").query();
	}

	public static TextField tagTextField() {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagEntryTextField").query();
	}

	public static Button addTagButton() {
		return FxAssert.assertContext().getNodeFinder().lookup("#addTagButton").query();
	}

	public static Button removeTag(int idx) {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagsTable").lookup(".button").nth(idx).query();
	}

	public static Button calculateTagsDeltaButton() {
		return FxAssert.assertContext().getNodeFinder().lookup("#calculateTagsDeltaButton").query();
	}
}
