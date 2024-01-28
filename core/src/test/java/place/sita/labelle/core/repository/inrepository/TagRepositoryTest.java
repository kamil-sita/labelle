package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
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
		context.delete(Tables.TAG_SRC).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY).execute();

		tagRepository.invalidateCaches();
	}

	@Test
	public void shouldAddTag() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).value()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
	}

	@Test
	public void shouldAddMultipleTags() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");
		tagRepository.addTag(imageId, repo.id(), "Some tag 2", "Some family 2");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags.get(0).value()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
		assertThat(tags.get(1).value()).isEqualTo("Some tag 2");
		assertThat(tags.get(1).family()).isEqualTo("Some family 2");
	}

	@Test
	public void shouldAddMultipleTagsWithinSameFamily() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");
		tagRepository.addTag(imageId, repo.id(), "Some tag 2", "Some family");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags.get(0).value()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
		assertThat(tags.get(1).value()).isEqualTo("Some tag 2");
		assertThat(tags.get(1).family()).isEqualTo("Some family");
	}

	@Test
	public void shouldAddingSameTagBeIdempotent() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(1);
		assertThat(tags.get(0).value()).isEqualTo("Some tag");
		assertThat(tags.get(0).family()).isEqualTo("Some family");
	}

	@Test
	public void shouldDeleteTagFromImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		tagRepository.addTag(imageId, repo.id(), "Some tag", "Some family");

		// when
		tagRepository.deleteTag(imageId, null, "Some tag", "Some family");

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
		persistableImagesTags.addTag(imageId, "Some tag", "Some family");
		persistableImagesTags.addTag(imageId, "Some tag 2", "Some family 2");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new TagRepository.TagView("Some tag", "Some family"));
		assertThat(tags).contains(new TagRepository.TagView("Some tag 2", "Some family 2"));
	}

	@Test
	public void shouldAddMultipleTagsUsingPersistableImageTagsWithKnownRepoId() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags(repo.id());
		persistableImagesTags.addTag(imageId, "Some tag", "Some family");
		persistableImagesTags.addTag(imageId, "Some tag 2", "Some family 2");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new TagRepository.TagView("Some tag", "Some family"));
		assertThat(tags).contains(new TagRepository.TagView("Some tag 2", "Some family 2"));
	}

}
