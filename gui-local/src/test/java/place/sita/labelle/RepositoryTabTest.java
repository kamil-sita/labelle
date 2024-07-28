package place.sita.labelle;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import place.sita.labelle.actions.RepositoryActions;
import place.sita.labelle.actions.TabActions;
import place.sita.labelle.core.images.imagelocator.ImageLocatorService;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.state.StateChange.withAction;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.toBeTrueAfterAction;

@ExtendWith(ApplicationExtension.class)
public class RepositoryTabTest extends GuiTest {
	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private ImageLocatorService imageLocatorService;

	@Start
	public void start(Stage stage) {
		setUnstableSceneReporter(stageConfiguration.configureTestStage(stage));
	}

	@Test
	public void shouldBeAbleToAddAndRemoveRepository(FxRobot robot) {
		var repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/test1/");
		imageLocatorService.createRoot("C:/test2/");
		var img1 = inRepositoryService.images().addImage(repo.id(), "C:/test1/test.jpg").getSuccess();
		var img2 = inRepositoryService.images().addImage(repo.id(), "C:/test2/test.jpg").getSuccess();
		inRepositoryService.addTag(img1.id(), new Tag("category 1", "tag 1"));
		inRepositoryService.addTag(img1.id(), new Tag("category 1", "tag 2"));
		inRepositoryService.addTag(img2.id(), new Tag("category 2", "tag 1"));
		inRepositoryService.addTag(img2.id(), new Tag("category 2", "tag 2"));

		// visit Repositories tab
		withAction(() -> {
			robot.clickOn(TabActions.getMainTab("Repository"));
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		// todo code above unreliably fails
		robot.sleep(1, TimeUnit.SECONDS);

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);

		robot.sleep(1, TimeUnit.SECONDS); // todo

		// check state
		FxAssert.verifyThat(RepositoryActions.paginatorList(), ListViewMatchers.hasItems(2));

		robot.clickOn(RepositoryActions.filteringTextArea());
		robot.type(KeyCode.END);
		robot.type(KeyCode.ENTER);
		robot.write("IN tags EXISTS (tag");
		robot.sleep(1, TimeUnit.SECONDS); // todo
		FxAssert.verifyThat(RepositoryActions.paginatorList(), ListViewMatchers.hasItems(2));
		assertThat(RepositoryActions.filteringTextAreaFeedback().getText()).startsWith("Validation failed");
		robot.write("= \"tag 1\")");
		robot.sleep(1, TimeUnit.SECONDS); // todo
		FxAssert.verifyThat(RepositoryActions.paginatorList(), ListViewMatchers.hasItems(2));
		assertThat(RepositoryActions.filteringTextAreaFeedback().getText()).startsWith("Validation OK");
		robot.write("AND IN tags EXISTS (category = \"category 1\")");
		robot.sleep(1, TimeUnit.SECONDS); // todo
		FxAssert.verifyThat(RepositoryActions.paginatorList(), ListViewMatchers.hasItems(1));
		assertThat(RepositoryActions.filteringTextAreaFeedback().getText()).startsWith("Validation OK");

	}
}
