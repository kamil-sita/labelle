package place.sita.labelle.actions;

import javafx.scene.control.TextArea;
import org.testfx.api.FxAssert;

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

}
