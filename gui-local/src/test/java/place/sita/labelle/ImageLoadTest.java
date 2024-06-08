package place.sita.labelle;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import place.sita.labelle.actions.*;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;
import place.sita.labelle.jooq.Tables;
import place.sita.labelle.state.StateChange;
import place.sita.labelle.state.assertions.SimpleChangeAssertion;
import place.sita.magicscheduler.scheduler.SchedulerStatistics;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.state.StateChange.withAction;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.changeIn;
import static place.sita.labelle.state.assertions.SimpleChangeAssertion.nonNullChangeIn;

@ExtendWith(ApplicationExtension.class)
public class ImageLoadTest extends TestContainersTest {
	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private SchedulerStatistics schedulerStatistics;

	@Autowired
	private DSLContext context;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_DELTA).execute();

		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_SRC).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();

		context.delete(Tables.IMAGE_FILE).execute();
		context.delete(Tables.ROOT).execute();
	}

	@Start
	public void start(Stage stage) {
		stageConfiguration.configureStage(stage);
	}

	@Test
	public void shouldLoadImage(FxRobot robot) throws IOException {
		File file = new File("../images/splash.png");
		File canonicalFile = file.getCanonicalFile();
		assertThat(canonicalFile.exists()).isTrue();
		String root = canonicalFile.getParentFile().getAbsolutePath();

		// visit Repositories tab
		Node node = TabActions.getMainTab("Repositories");

		robot.clickOn(node);
		robot.sleep(1, TimeUnit.SECONDS);

		// check if list is empty
		ListView repositoryList = RepositoriesActions.getRepositoryList();

		FxAssert.verifyThat(repositoryList, ListViewMatchers.isEmpty());
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(0);

		// add repository
		RepositoriesActions.createNewRepository(robot, "My test repository");

		UUID repositoryId = repositoryService.getRepositories(null).get(0).id();
		// check state
		FxAssert.verifyThat(repositoryList, ListViewMatchers.hasItems(1));
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(1);
		assertThat(repositoryService.getRepositories(null).get(0).name()).isEqualTo("My test repository");


		Node rootsTab = TabActions.getMainTab("Data roots");

		robot.clickOn(rootsTab);
		robot.sleep(1, TimeUnit.SECONDS);

		DataRootsActions.createDataRoot(robot, root);


		// schedule adding an image
		Node schedulerExecutorTab = TabActions.getMainTab("Scheduler Executor");

		robot.clickOn(schedulerExecutorTab);
		robot.sleep(1, TimeUnit.SECONDS);

		SchedulerExecutorActions.filterBy(robot, "add-image-v1");
		FxAssert.verifyThat(SchedulerExecutorActions.taskList(), ListViewMatchers.hasItems(1));
		robot.clickOn(SchedulerExecutorActions.schedulerExecutorTask(0));

		robot.clickOn(SchedulerExecutorActions.clearCodeArea());
		robot.clickOn(SchedulerExecutorActions.codeArea());
		String json = """
			{
				"repoId": "%s",
				"path": "%s"
			}
			"""
			.formatted(repositoryId.toString(), canonicalFile.getAbsolutePath().replace("\\", "\\\\"));

		robot.write(json);
		withAction(() -> {
			robot.clickOn(SchedulerExecutorActions.executeButton());
		})
			.expect(changeIn(() -> schedulerStatistics.successfulTaskCount()))
			.test();

		Node repositoryTab = TabActions.getMainTab("Repository");
		robot.clickOn(repositoryTab);
		robot.sleep(1, TimeUnit.SECONDS);

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);

		robot.sleep(1, TimeUnit.SECONDS);
		withAction(() -> {
			robot.clickOn(RepositoryActions.imageInPaginator(0));
		})
			.expect(nonNullChangeIn(() -> {
				ImageView imageView = RepositoryActions.imageView();
				return imageView.getImage();
			}))
			.test();
	}
}
