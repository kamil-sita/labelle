package place.sita.labelle.actions;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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

	public static ListView paginatorList() {
		return FxAssert.assertContext().getNodeFinder().lookup("#paginator").lookup(".list-view").query();
	}

	public static TextField categoryTextField() {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagCategoryTextField").query();
	}

	public static TextField tagTextField() {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagTagTextField").query();
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

	public static ImageView imageView() {
		return FxAssert.assertContext().getNodeFinder().lookup("#imageView").query();
	}

	public static TextArea filteringTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#filteringTextArea").query();
	}

	public static TextField filteringTextAreaFeedback() {
		return FxAssert.assertContext().getNodeFinder().lookup("#filteringTextAreaFeedback").query();
	}
}
