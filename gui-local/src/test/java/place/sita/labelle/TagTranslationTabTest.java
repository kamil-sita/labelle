package place.sita.labelle;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import place.sita.labelle.actions.RepositoryActions;
import place.sita.labelle.actions.TabActions;
import place.sita.labelle.actions.TagTranslationActions;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.state.StateChange.withAction;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.toBeTrueAfterAction;

@ExtendWith(ApplicationExtension.class)
public class TagTranslationTabTest extends GuiTest {

	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Start
	public void start(Stage stage) {
		setUnstableSceneReporter(stageConfiguration.configureTestStage(stage));
	}

	@Test
	public void shouldValidateTagTranslation(FxRobot robot) {
		// visit tag translation
		Node node = TabActions.getMainTab("Tag translation");

		withAction(() -> {
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		// perform tag translation
		withAction(() -> {
			robot.clickOn(TagTranslationActions.tagLevelRulesTextArea());
			robot.type(KeyCode.END);
			robot.write("Something");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		String validationText = TagTranslationActions.validationResultsTextArea().getText();
		assertThat(validationText).startsWith("Validation failed");

		// perform tag translation
		withAction(() -> {
			for (int i = 0; i < 9; i++) {
				robot.type(KeyCode.BACK_SPACE);
			}
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		validationText = TagTranslationActions.validationResultsTextArea().getText();
		assertThat(validationText).startsWith("Validation OK");
	}

	@Test
	public void shouldPerformTagTranslation(FxRobot robot) {
		// visit tag translation
		Node node = TabActions.getMainTab("Tag translation");

		withAction(() -> {
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();
		// add some new tags in
		withAction(() -> {
			robot.clickOn(TagTranslationActions.testTagsBeforeTextArea());
			robot.type(KeyCode.END);
			robot.type(KeyCode.ENTER);
			robot.write("Test category;Test tag");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		String validationText = TagTranslationActions.validationResultsTextArea().getText();
		assertThat(validationText).startsWith("Validation OK");

		String newTags = TagTranslationActions.testTagsAfterTextArea().getText();
		assertThat(newTags).contains("Test result;Passed");
	}

	@Test
	public void shouldSaveNewTagTranslation(FxRobot robot) {
		repositoryService.addRepository("Test repository");

		// visit tag translation
		Node node = TabActions.getMainTab("Tag translation");

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isEqualTo("Changing repo will lose unsaved changes");
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isNullOrEmpty();

		withAction(() -> {
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);
		robot.sleep(1, TimeUnit.SECONDS); // todo - rewrite to stability action framework

		assertThat(TagTranslationActions.tagLevelRulesTextArea().getText()).isEmpty();
		assertThat(TagTranslationActions.containerLevelRulesTextArea().getText()).isEmpty();

		withAction(() -> {
			robot.clickOn(TagTranslationActions.tagLevelRulesTextArea());
			robot.write("IF tag in (\"Tag 1\", \"Tag2\") THEN ADD ");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isEqualTo("Changing repo will lose unsaved changes");
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isEqualTo("Validation failed - those rules cannot be executed.");

		withAction(() -> {
			robot.clickOn(TagTranslationActions.tagLevelRulesTextArea());
			robot.write("(\"Xyz\", MATCHED)");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isEqualTo("Changing repo will lose unsaved changes");
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isNullOrEmpty();

		withAction(() -> {
			robot.clickOn(TagTranslationActions.containerLevelRulesTextArea());
			robot.write("//Some test");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isEqualTo("Changing repo will lose unsaved changes");
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isNullOrEmpty();

		withAction(() -> {
			robot.clickOn(TagTranslationActions.saveButton());
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isNullOrEmpty();
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isNullOrEmpty();

		withAction(() -> {
			robot.clickOn(TagTranslationActions.containerLevelRulesTextArea());
			robot.write("//Some more text");
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		assertThat(TagTranslationActions.unsavedChangesLabel().getText()).isEqualTo("Changing repo will lose unsaved changes");
		assertThat(TagTranslationActions.validationFailedLabel().getText()).isNullOrEmpty();

		withAction(() -> {
			// temporarily visit another tab to cause reload
			robot.clickOn(TabActions.getMainTab("Repository"));
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);
		robot.sleep(1, TimeUnit.SECONDS); // todo - rewrite to stability action framework

		assertThat(TagTranslationActions.tagLevelRulesTextArea().getText()).isEqualTo("IF tag in (\"Tag 1\", \"Tag2\") THEN ADD (\"Xyz\", MATCHED)");
		assertThat(TagTranslationActions.containerLevelRulesTextArea().getText()).isEqualTo("//Some test");
	}
}
