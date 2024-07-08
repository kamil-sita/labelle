package place.sita.labelle.core.tasks;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.InRepositoryService.TagResponse;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;
import place.sita.magicscheduler.scheduler.TaskStateContext;
import place.sita.magicscheduler.scheduler.environment.TaskExecutionEnvironment;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MassTagTaskTypeTest extends TestContainersTest {
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

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();
	}

	@Test
	public void shouldMassTagRepositoryWithoutImages() {
		// given
		Repository repository = repositoryService.addRepository("Test repo");

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new MassTagTaskType(),
			new MassTagTaskType.Config(repository.id(), "family", "tag"),
			new TaskStateContext(false)
		);

		// then
		assertThat(results.exception()).isNull();
	}

	@Test
	public void shouldMassTagRepositoryWithImages() {
		// given
		Repository repository = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repository.id());

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new MassTagTaskType(),
			new MassTagTaskType.Config(repository.id(), "family", "tag"),
			new TaskStateContext(false)
		);

		// then
		assertThat(results.exception()).isNull();
		List<TagResponse> tags = inRepositoryService.getTags(imageId);
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).family()).isEqualTo("family");
		assertThat(tags.get(0).tag()).isEqualTo("tag");
	}

	@Test
	public void shouldMassTagRepositoryWithMultipleImages() {
		// given
		Repository repository = repositoryService.addRepository("Test repo");
		UUID imageId1 = inRepositoryService.addEmptySyntheticImage(repository.id());
		UUID imageId2 = inRepositoryService.addEmptySyntheticImage(repository.id());

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new MassTagTaskType(),
			new MassTagTaskType.Config(repository.id(), "family", "tag"),
			new TaskStateContext(false)
		);

		// then
		assertThat(results.exception()).isNull();
		List<TagResponse> tags1 = inRepositoryService.getTags(imageId1);
		assertThat(tags1).hasSize(1);
		assertThat(tags1.get(0).family()).isEqualTo("family");
		assertThat(tags1.get(0).tag()).isEqualTo("tag");
		List<TagResponse> tags2 = inRepositoryService.getTags(imageId2);
		assertThat(tags2).hasSize(1);
		assertThat(tags2.get(0).family()).isEqualTo("family");
		assertThat(tags2.get(0).tag()).isEqualTo("tag");
	}

	@Test
	public void shouldMassTagRepositoryWithExistingTagsWithoutOverridingThem() {
		// given
		Repository repository = repositoryService.addRepository("Test repo");
		UUID firstImage = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(firstImage, null, "tag", "family");
		inRepositoryService.addTag(firstImage, null, "some other tag", "family");
		UUID secondImage = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(secondImage, null, "another tag", "of different family");

		// when
		var results = taskExecutionEnvironment.executeTask(
			UUID.randomUUID(),
			new MassTagTaskType(),
			new MassTagTaskType.Config(repository.id(), "family", "tag"),
			new TaskStateContext(false)
		);

		// then
		assertThat(results.exception()).isNull();
		List<TagResponse> tags1 = inRepositoryService.getTags(firstImage);
		assertThat(tags1).hasSize(2);
		assertThat(tags1).contains(new TagResponse("tag", "family"));
		assertThat(tags1).contains(new TagResponse("some other tag", "family"));
		List<TagResponse> tags2 = inRepositoryService.getTags(secondImage);
		assertThat(tags2).hasSize(2);
		assertThat(tags2).contains(new TagResponse("another tag", "of different family"));
		assertThat(tags2).contains(new TagResponse("tag", "family"));
	}
}
