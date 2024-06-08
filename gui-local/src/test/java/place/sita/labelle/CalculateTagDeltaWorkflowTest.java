package place.sita.labelle;

import javafx.scene.Node;
import javafx.scene.control.ListView;
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
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.delta.TagDeltaResponse;
import place.sita.labelle.core.repository.inrepository.delta.TagDeltaType;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.StageConfiguration;
import place.sita.labelle.jooq.Tables;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class CalculateTagDeltaWorkflowTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private StageConfiguration stageConfiguration;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private RootRepository rootRepository;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private CacheRegistry cacheRegistry;

	@Start
	public void start(Stage stage) {
		stageConfiguration.configureStage(stage);
	}

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
		cacheRegistry.invalidate();
	}

	@Test
	public void shouldGoThroughTagDeltaWorkflow(FxRobot robot) {
		Node rootsTab = TabActions.getMainTab("Data roots");

		robot.clickOn(rootsTab);
		robot.sleep(1, TimeUnit.SECONDS);

		FxAssert.verifyThat(DataRootsActions.getRootList(), ListViewMatchers.isEmpty());
		assertThat(rootRepository.getRoots()).isEmpty();

		DataRootsActions.createDataRoot(robot, "C:/test_data_root/");

		FxAssert.verifyThat(DataRootsActions.getRootList(), ListViewMatchers.hasItems(1));
		assertThat(rootRepository.getRoots()).hasSize(1);
		assertThat(rootRepository.getRoots().get(0).directory()).isEqualTo("C:/test_data_root/");

		// visit Repositories tab
		Node repositoriesTab = TabActions.getMainTab("Repositories");

		robot.clickOn(repositoriesTab);
		robot.sleep(1, TimeUnit.SECONDS);

		ListView repositoryList = RepositoriesActions.getRepositoryList();

		FxAssert.verifyThat(repositoryList, ListViewMatchers.isEmpty());
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(0);

		// add repository
		RepositoriesActions.createNewRepository(robot, "My test repository");

		FxAssert.verifyThat(repositoryList, ListViewMatchers.hasItems(1));
		assertThat(repositoryService.getRepositories(null).size()).isEqualTo(1);
		assertThat(repositoryService.getRepositories(null).get(0).name()).isEqualTo("My test repository");

		UUID repositoryId = repositoryService.getRepositories(null).get(0).id();
		// todo make the below work
		//FxAssert.verifyThat(RepositoryActions.uuidField(), TextInputControlMatchers.hasText(repositoryId.toString()));

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
				"path": "C:/test_data_root/image_1.png"
			}
			"""
			.formatted(repositoryId.toString());

		robot.write(json);
		robot.clickOn(SchedulerExecutorActions.executeButton());
		robot.sleep(5, TimeUnit.SECONDS);

		Node repositoryTab = TabActions.getMainTab("Repository");
		robot.clickOn(repositoryTab);
		robot.sleep(1, TimeUnit.SECONDS);

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);

		robot.sleep(1, TimeUnit.SECONDS);
		robot.clickOn(RepositoryActions.imageInPaginator(0));
		robot.clickOn(RepositoryActions.familyTextField());
		robot.write("Test Family");
		robot.clickOn(RepositoryActions.tagTextField());
		robot.write("Test Tag");
		robot.clickOn(RepositoryActions.addTagButton());

		robot.clickOn(schedulerExecutorTab);
		robot.sleep(1, TimeUnit.SECONDS);

		SchedulerExecutorActions.filterBy(robot, "create-child-repo-v1");
		FxAssert.verifyThat(SchedulerExecutorActions.taskList(), ListViewMatchers.hasItems(1));
		robot.clickOn(SchedulerExecutorActions.schedulerExecutorTask(0));
		robot.clickOn(SchedulerExecutorActions.clearCodeArea());
		robot.clickOn(SchedulerExecutorActions.codeArea());

		json = """
				{
				  "parents" : [ "%s" ],
				  "newRepositoryName" : "My test repository - child"
				}
			"""
			.formatted(repositoryId.toString());

		robot.write(json);
		robot.clickOn(SchedulerExecutorActions.executeButton());
		robot.sleep(5, TimeUnit.SECONDS);

		robot.clickOn(repositoryTab);
		robot.sleep(1, TimeUnit.SECONDS);

		robot.clickOn(RepositoryActions.repositoryChoiceBox());
		robot.type(KeyCode.DOWN);
		robot.type(KeyCode.ENTER);
		robot.sleep(1, TimeUnit.SECONDS);
		robot.clickOn(RepositoryActions.imageInPaginator(0));
		robot.sleep(1, TimeUnit.SECONDS);
		robot.clickOn(RepositoryActions.removeTag(0));

		robot.clickOn(RepositoryActions.familyTextField());
		robot.write("Test Family 2");
		robot.clickOn(RepositoryActions.tagTextField());
		robot.write("Test Tag 2");
		robot.clickOn(RepositoryActions.addTagButton());
		robot.clickOn(RepositoryActions.calculateTagsDeltaButton());
		robot.sleep(1, TimeUnit.SECONDS);

		List<TagDeltaResponse> deltas = inRepositoryService.tagDeltas().getAll();
		assertThat(deltas).hasSize(2);
		assertThat(deltas).contains(new TagDeltaResponse("Test Family", "Test Tag", TagDeltaType.REMOVE));
		assertThat(deltas).contains(new TagDeltaResponse("Test Family 2", "Test Tag 2", TagDeltaType.ADD));
	}

}
