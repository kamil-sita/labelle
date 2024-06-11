package place.sita.labelle;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import place.sita.labelle.actions.RepositoriesActions;
import place.sita.labelle.actions.TabActions;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.state.StateChange.withAction;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.toBeTrueAfterAction;

@ExtendWith(ApplicationExtension.class)
public class RepositoryTabTest extends GuiTest {
	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Start
	public void start(Stage stage) {
		setUnstableSceneReporter(stageConfiguration.configureTestStage(stage));
	}

	@Test
	public void shouldBeAbleToAddAndRemoveRepository(FxRobot robot) {
		// visit Repositories tab
		Node node = TabActions.getMainTab("Repositories");

		withAction(() -> {
			robot.clickOn(node);
		})
			.expect(toBeTrueAfterAction(() -> unstableSceneReporter.isStable()))
			.test();

		// check if list is empty
		ListView repositoryList = RepositoriesActions.getRepositoryList();

		FxAssert.verifyThat(repositoryList, ListViewMatchers.isEmpty());
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(0);

		// add repository
		RepositoriesActions.createNewRepository(robot, "My test repository");

		// check state
		FxAssert.verifyThat(repositoryList, ListViewMatchers.hasItems(1));
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(1);
		assertThat(repositoryService.getRepositories(null).get(0).name()).isEqualTo("My test repository");

		// delete repository
		robot.clickOn(RepositoriesActions.repository(0));
		robot.clickOn(RepositoriesActions.deleteRepositoryButton());

		// check state
		FxAssert.verifyThat(repositoryList, ListViewMatchers.isEmpty());
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(0);
	}
}
