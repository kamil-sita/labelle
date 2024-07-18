package place.sita.labelle.actions;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.testfx.api.FxAssert;
import org.testfx.service.query.NodeQuery;

public class TagTranslationActions {

	public static TextArea tagLevelRulesTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#tagLevelRulesTextArea").query();
	}

	public static TextArea containerLevelRulesTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#containerLevelRulesTextArea").query();
	}

	public static TextArea validationResultsTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#validationResultsTextArea").query();
	}

	public static TextArea testTagsBeforeTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#testTagsBeforeTextArea").query();
	}

	public static TextArea testTagsAfterTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#testTagsAfterTextArea").query();
	}

	public static ChoiceBox repositoryChoiceBox() {
		return repositoryChoiceBoxQuery().query();
	}

	public static NodeQuery repositoryChoiceBoxQuery() {
		return FxAssert.assertContext().getNodeFinder().lookup("#repositoryChoiceBox");
	}

	public static Label unsavedChangesLabel() {
		return FxAssert.assertContext().getNodeFinder().lookup("#unsavedChangesLabel").query();
	}

	public static Label validationFailedLabel() {
		return FxAssert.assertContext().getNodeFinder().lookup("#validationFailedLabel").query();
	}

	public static Button saveButton() {
		return FxAssert.assertContext().getNodeFinder().lookup("#saveButton").query();
	}
}
