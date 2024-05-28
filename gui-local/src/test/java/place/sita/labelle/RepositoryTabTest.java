package place.sita.labelle;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class RepositoryTabTest extends TestContainersTest {
	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Start
	public void start(Stage stage) {
		stageConfiguration.configureStage(stage);
	}

	@Test
	public void shouldBeAbleToAddAndRemoveRepository(FxRobot robot) {
		// visit Repositories tab
		Node node = FxAssert.assertContext().getNodeFinder()
			.lookup("#mainTabPane")
			.lookup(".tab-header-area > .headers-region > .tab")
			.lookup("Repositories").query();

		robot.clickOn(node);
		robot.sleep(1, TimeUnit.SECONDS);

		// check if list is empty
		ListView repositoryList = FxAssert.assertContext().getNodeFinder()
			.lookup("#repositoryList").query();

		FxAssert.verifyThat(repositoryList, ListViewMatchers.isEmpty());
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(0);

		// add repository
		TextField testField = FxAssert.assertContext().getNodeFinder()
			.lookup("#repositoryNameTextField").query();

		robot.clickOn(testField);
		robot.write("My test repository");
		robot.clickOn("#addNewButton");

		// check state
		FxAssert.verifyThat(repositoryList, ListViewMatchers.hasItems(1));
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(1);
	}
}
