package place.sita.labelle.core.automation.tasks.clone;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.environment.TaskExecutionEnvironment;
import place.sita.labelle.jooq.Tables;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CloneRepositoryTaskTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private TaskExecutionEnvironment taskExecutionEnvironment;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_SRC).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY).execute();
	}

	@Test
	public void shouldCloneRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, null, "Some tag", "Some family");

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new CloneRepositoryTask(),
			new CloneRepositoryTaskInput(
				repo.id(),
				null,
				"New repo name",
				Map.of()
			),
			new TaskStateContext(false)
		);

		// then
		var images = inRepositoryService.images(results.result(), 0, Integer.MAX_VALUE, "");
		assertThat(images).hasSize(1);
		var tags = inRepositoryService.getTags(images.get(0).id());
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
	}


}
