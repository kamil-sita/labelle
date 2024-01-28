package place.sita.labelle.core.automation.tasks.clear;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.TagRepository;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.core.tasks.scheduler.TaskStateContext;
import place.sita.labelle.core.tasks.scheduler.environment.TaskExecutionEnvironment;
import place.sita.labelle.jooq.Tables;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ClearRepositoryTaskTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private TagRepository tagRepository;

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

		tagRepository.invalidateCaches();
	}

	@Test
	public void shouldClearRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, null, "Some tag", "Some family");
		UUID anotherImageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, null, "Some tag", "Some family");
		inRepositoryService.addTag(anotherImageId, null, "Some tag", "Some family 2");

		// when
		taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new ClearRepositoryTask(),
			new ClearRepositoryTaskInput(repo.id()),
			new TaskStateContext(false)
		);

		// then
		var images = inRepositoryService.images(repo.id(), 0, Integer.MAX_VALUE, "");
		assertThat(images).isEmpty();
	}

	@Test
	public void shouldNotClearUnrelatedRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, null, "Some tag", "Some family");
		UUID anotherImageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, null, "Some tag", "Some family");
		inRepositoryService.addTag(anotherImageId, null, "Some tag", "Some family 2");

		Repository unrelatedRepo = repositoryService.addRepository("Unrelated test repo");
		UUID imageInUnrelatedRepo = inRepositoryService.addEmptySyntheticImage(unrelatedRepo.id());

		// when
		taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new ClearRepositoryTask(),
			new ClearRepositoryTaskInput(repo.id()),
			new TaskStateContext(false)
		);

		// then
		var images = inRepositoryService.images(repo.id(), 0, Integer.MAX_VALUE, "");
		assertThat(images).isEmpty();

		var imagesInUnrelatedRepo = inRepositoryService.images(unrelatedRepo.id(), 0, Integer.MAX_VALUE, "");
		assertThat(imagesInUnrelatedRepo).isNotEmpty();
	}

}
