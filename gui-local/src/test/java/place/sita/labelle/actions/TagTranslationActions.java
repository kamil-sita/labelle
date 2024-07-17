package place.sita.labelle.actions;

import javafx.scene.control.TextArea;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;

public class TagTranslationActions {

	public static void testTagTranslation(FxRobot robot) {
		robot.clickOn("#doConversionButton");
	}

	public static TextArea beforeTranslationTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#testTagsBeforeTextArea").query();
	}

	public static TextArea afterTranslationTextArea() {
		return FxAssert.assertContext().getNodeFinder().lookup("#testTagsAfterTextArea").query();
	}

}
