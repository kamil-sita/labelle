package place.sita.labelle;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import place.sita.labelle.gui.local.StageConfiguration;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ExtendWith(ApplicationExtension.class)
public class MainMenuTest extends TestContainersTest {

	@Autowired
	private StageConfiguration stageConfiguration;

	@Start
	public void start(Stage stage) {
		stageConfiguration.configureStage(stage);
	}

	@Test
	public void shouldVisitAllTabs(FxRobot robot) {
		Set<Node> nodes = FxAssert.assertContext().getNodeFinder().lookup("#mainTabPane").lookup(".tab-header-area > .headers-region > .tab").queryAll();

		for (Node node : nodes) {
			robot.sleep(300, TimeUnit.MILLISECONDS);
			robot.clickOn(node);
			robot.sleep(300, TimeUnit.MILLISECONDS);
		}
	}
}
