package place.sita.labelle.core.repository.inrepository.tags;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TagRepositoryTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

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
	public void shouldAddTag() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).category()).isEqualTo("Some category");
	}

	@Test
	public void shouldAddMultipleTags() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");
		tagRepository.addTag(imageId, repo.id(), "Some tag 2", "Some category 2");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).category()).isEqualTo("Some category");
		assertThat(tags.get(1).tag()).isEqualTo("Some tag 2");
		assertThat(tags.get(1).category()).isEqualTo("Some category 2");
	}

	@Test
	public void shouldAddMultipleTagsWithinSameCategory() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");
		tagRepository.addTag(imageId, repo.id(), "Some tag 2", "Some category");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).category()).isEqualTo("Some category");
		assertThat(tags.get(1).tag()).isEqualTo("Some tag 2");
		assertThat(tags.get(1).category()).isEqualTo("Some category");
	}

	@Test
	public void shouldAddingSameTagBeIdempotent() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).tag()).isEqualTo("Some tag");
		assertThat(tags.get(0).category()).isEqualTo("Some category");
	}

	@Test
	public void shouldDeleteTagFromImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some category");

		// when
		tagRepository.deleteTag(imageId, null, "Some tag", "Some category");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).isEmpty();
	}

	@Test
	public void shouldAddMultipleTagsUsingPersistableImageTags() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId, "Some tag", "Some category");
		persistableImagesTags.addTag(imageId, "Some tag 2", "Some category 2");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new Tag("Some tag", "Some category"));
		assertThat(tags).contains(new Tag("Some tag 2", "Some category 2"));
	}

	@Test
	public void shouldAddMultipleTagsUsingPersistableImageTagsWithKnownRepoId() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags(repo.id());
		persistableImagesTags.addTag(imageId, "Some tag", "Some category");
		persistableImagesTags.addTag(imageId, "Some tag 2", "Some category 2");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new Tag("Some tag", "Some category"));
		assertThat(tags).contains(new Tag("Some tag 2", "Some category 2"));
	}

}
