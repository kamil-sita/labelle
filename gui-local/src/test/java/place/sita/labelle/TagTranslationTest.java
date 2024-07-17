package place.sita.labelle;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import place.sita.labelle.actions.TabActions;
import place.sita.labelle.actions.TagTranslationActions;
import place.sita.labelle.gui.local.StageConfiguration;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.state.StateChange.withAction;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.toBeTrueAfterAction;

@ExtendWith(ApplicationExtension.class)
public class TagTranslationTest extends GuiTest {

	@Autowired
	private StageConfiguration stageConfiguration;

	@Start
	public void start(Stage stage) {
		setUnstableSceneReporter(stageConfiguration.configureTestStage(stage));
	}

	@Test
	public void shouldPerformTagTranslation(FxRobot robot) throws IOException {
		// visit tag translation
		Node node = TabActions.getMainTab("Tag translation");

		withAction(() -> {
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		// perform tag translation
		withAction(() -> {
			TagTranslationActions.testTagTranslation(robot);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		// test results
		String results = TagTranslationActions.afterTranslationTextArea().getText();
		assertThat(results).isEqualToIgnoringNewLines(
			"""
				Category 1;Tag 1
				Category 2;Tag 2
				Category 3;Tag 3
				Xyz;Tag 1
				Cat1;Tag1
				"""
		);
	}
}
