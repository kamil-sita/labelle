package place.sita.labelle.core.automation.tasks.clear;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.environment.TaskExecutionEnvironment;
import place.sita.labelle.jooq.Tables;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClearRepositoryTaskTest extends TestContainersTest {

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
		context.delete(Tables.TAG_CATEGORY).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY).execute();
	}

	@Test
	public void shouldClearRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("Some category", "Some tag"));
		UUID anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("Some category", "Some tag"));
		inRepositoryService.addTag(anotherImageId, new Tag("Some category 2", "Some tag"));

		// when
		taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new ClearRepositoryTask(),
			new ClearRepositoryTaskInput(repo.id()),
			new TaskStateContext(false)
		);

		// then
		int imageCount = (inRepositoryService.images().images()).count();
		assertThat(imageCount).isZero();
	}

	@Test
	public void shouldNotClearUnrelatedRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("Some category", "Some tag"));
		UUID anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("Some category", "Some tag"));
		inRepositoryService.addTag(anotherImageId, new Tag("Some category 2", "Some tag"));

		Repository unrelatedRepo = repositoryService.addRepository("Unrelated test repo");
		inRepositoryService.images().addEmptySyntheticImage(unrelatedRepo.id());

		// when
		taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new ClearRepositoryTask(),
			new ClearRepositoryTaskInput(repo.id()),
			new TaskStateContext(false)
		);

		// then
		int imageCount = (inRepositoryService.images().images()).process().filterByRepository(repo.id()).count();
		assertThat(imageCount).isZero();

		int imageCountInUnrelatedRepo = (inRepositoryService.images().images()).process().filterByRepository(unrelatedRepo.id()).count();
		assertThat(imageCountInUnrelatedRepo).isOne();
	}

}
