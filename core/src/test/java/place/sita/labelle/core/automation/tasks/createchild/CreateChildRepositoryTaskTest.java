package place.sita.labelle.core.automation.tasks.createchild;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.automation.tasks.clone.CloneRepositoryTask;
import place.sita.labelle.core.automation.tasks.clone.CloneRepositoryTaskInput;
import place.sita.labelle.core.repository.inrepository.Ids;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.TagRepository;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.environment.TaskExecutionEnvironment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateChildRepositoryTaskTest extends TestContainersTest {

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
		context.delete(Tables.TAG_DELTA).execute();
		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_SRC).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();
	}

	@Test
	public void shouldCreateChildRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		Ids parentIds = inRepositoryService.getIds(imageId);
		inRepositoryService.addTag(imageId, null, "Some tag", "Some family");

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new CreateChildRepositoryTask(),
			new CreateChildRepositoryTaskInput(
				List.of(repo.id()),
				null,
				"New repo name"
			),
			new TaskStateContext(false)
		);

		// then
		List<Repository> repos = repositoryService.getRepositories(null);
		assertThat(repos).hasSize(2);
		assertThat(repos.stream().map(Repository::name).toList()).contains("New repo name");
		var images = inRepositoryService.images(results.result(), 0, Integer.MAX_VALUE, "");
		assertThat(images).hasSize(1);
		var tags = inRepositoryService.getTags(images.get(0).id());
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
		Ids imageIds = inRepositoryService.getIds(images.get(0).id());
		assertThat(imageIds.persistentId()).isEqualTo(parentIds.persistentId());
		assertThat(imageIds.parentPersistentId()).isEqualTo(parentIds.persistentId());
	}

	@Test
	public void shouldCreateChildRepositoryWithTagsFromBothRepos() {
		// given
		Repository firstParentRepo = repositoryService.addRepository("First parent repo");
		UUID firstParentImageId = inRepositoryService.addEmptySyntheticImage(firstParentRepo.id());
		inRepositoryService.addTag(firstParentImageId, null, "First tag", "First family");
		inRepositoryService.setPersistentId(firstParentImageId, "persistent ID");

		Repository secondParentRepo = repositoryService.addRepository("Second parent repo");
		UUID secondParentImageId = inRepositoryService.addEmptySyntheticImage(secondParentRepo.id());
		inRepositoryService.addTag(secondParentImageId, null, "Second tag", "Second family");
		inRepositoryService.setPersistentId(secondParentImageId, "persistent ID");

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new CreateChildRepositoryTask(),
			new CreateChildRepositoryTaskInput(
				List.of(firstParentRepo.id(), secondParentRepo.id()),
				null,
				"New repo name"
			),
			new TaskStateContext(false)
		);

		// then
		List<Repository> repos = repositoryService.getRepositories(null);
		assertThat(repos).hasSize(3);
		assertThat(repos.stream().map(Repository::name).toList()).contains("New repo name");
		var images = inRepositoryService.images(results.result(), 0, Integer.MAX_VALUE, "");
		assertThat(images).hasSize(1);
		var tags = inRepositoryService.getTags(images.get(0).id());
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new InRepositoryService.TagResponse("First tag", "First family"));
		assertThat(tags).contains(new InRepositoryService.TagResponse("Second tag", "Second family"));
		Ids imageIds = inRepositoryService.getIds(images.get(0).id());
		assertThat(imageIds.persistentId()).isEqualTo("persistent ID");
		assertThat(imageIds.parentPersistentId()).isEqualTo("persistent ID");
	}

	@Test
	public void shouldCreateChildRepositoryWithoutImagesIfImagesAreNotVisibleOutsideTheRepo() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, null, "Some tag", "Some family");
		inRepositoryService.setVisibility(imageId, false);

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new CreateChildRepositoryTask(),
			new CreateChildRepositoryTaskInput(
				List.of(repo.id()),
				null,
				"New repo name"
			),
			new TaskStateContext(false)
		);

		// then
		List<Repository> repos = repositoryService.getRepositories(null);
		assertThat(repos).hasSize(2);
		assertThat(repos.stream().map(Repository::name).toList()).contains("New repo name");
		var images = inRepositoryService.images(results.result(), 0, Integer.MAX_VALUE, "");
		assertThat(images).isEmpty();
	}

}
