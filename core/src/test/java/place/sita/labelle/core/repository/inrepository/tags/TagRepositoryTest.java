package place.sita.labelle.core.repository.inrepository.tags;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TagRepositoryTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@SpyBean
	private TagRepositoryProperties tagRepositoryProperties;

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));
		tagRepository.addTag(imageId, new Tag("Some category 2", "Some tag 2"));

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag 2"));

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));

		// when
		tagRepository.deleteTag(imageId, null, new Tag("Some category", "Some tag"));

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).isEmpty();
	}

	@Test
	public void shouldAddMultipleTagsUsingPersistableImageTags() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId, "Some category 2", "Some tag 2");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(2);
		assertThat(tags).contains(new Tag("Some category", "Some tag"));
		assertThat(tags).contains(new Tag("Some category 2", "Some tag 2"));
	}

	@Test
	public void shouldAddNumberOfTagsEqualToBulkSize() {
		// given
		when(tagRepositoryProperties.getTagBulkSize()).thenReturn(5);
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId, "Some category 2", "Some tag 2");
		persistableImagesTags.addTag(imageId, "Some category 3", "Some tag 3");
		persistableImagesTags.addTag(imageId, "Some category 4", "Some tag 4");
		persistableImagesTags.addTag(imageId, "Some category 5", "Some tag 5");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(5);
		assertThat(tags).contains(new Tag("Some category", "Some tag"));
		assertThat(tags).contains(new Tag("Some category 2", "Some tag 2"));
		assertThat(tags).contains(new Tag("Some category 3", "Some tag 3"));
		assertThat(tags).contains(new Tag("Some category 4", "Some tag 4"));
		assertThat(tags).contains(new Tag("Some category 5", "Some tag 5"));
	}

	@Test
	public void shouldAddNumberOfTagsBiggerThanBulkSize() {
		// given
		when(tagRepositoryProperties.getTagBulkSize()).thenReturn(5);
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId, "Some category 2", "Some tag 2");
		persistableImagesTags.addTag(imageId, "Some category 3", "Some tag 3");
		persistableImagesTags.addTag(imageId, "Some category 4", "Some tag 4");
		persistableImagesTags.addTag(imageId, "Some category 5", "Some tag 5");
		persistableImagesTags.addTag(imageId, "Some category 6", "Some tag 6");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags = tagRepository.getTags(imageId);
		assertThat(tags).hasSize(6);
		assertThat(tags).contains(new Tag("Some category", "Some tag"));
		assertThat(tags).contains(new Tag("Some category 2", "Some tag 2"));
		assertThat(tags).contains(new Tag("Some category 3", "Some tag 3"));
		assertThat(tags).contains(new Tag("Some category 4", "Some tag 4"));
		assertThat(tags).contains(new Tag("Some category 5", "Some tag 5"));
		assertThat(tags).contains(new Tag("Some category 6", "Some tag 6"));
	}

	@Test
	public void shouldTagNumberOfImagesEqualToBulkSize() {
		// given
		when(tagRepositoryProperties.getImageBulkSize()).thenReturn(5);
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId1 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId2 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId3 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId4 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId5 = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId1, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId2, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId3, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId4, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId5, "Some category", "Some tag");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags1 = tagRepository.getTags(imageId1);
		assertThat(tags1).hasSize(1);
		assertThat(tags1).contains(new Tag("Some category", "Some tag"));

		var tags2 = tagRepository.getTags(imageId2);
		assertThat(tags2).hasSize(1);
		assertThat(tags2).contains(new Tag("Some category", "Some tag"));

		var tags3 = tagRepository.getTags(imageId3);
		assertThat(tags3).hasSize(1);
		assertThat(tags3).contains(new Tag("Some category", "Some tag"));

		var tags4 = tagRepository.getTags(imageId4);
		assertThat(tags4).hasSize(1);
		assertThat(tags4).contains(new Tag("Some category", "Some tag"));

		var tags5 = tagRepository.getTags(imageId5);
		assertThat(tags5).hasSize(1);
		assertThat(tags5).contains(new Tag("Some category", "Some tag"));
	}

	@Test
	public void shouldTagNumberOfImagesBiggerThanBulkSize() {
		// given
		when(tagRepositoryProperties.getImageBulkSize()).thenReturn(5);
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId1 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId2 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId3 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId4 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId5 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId6 = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId1, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId2, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId3, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId4, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId5, "Some category", "Some tag");
		persistableImagesTags.addTag(imageId6, "Some category", "Some tag");
		tagRepository.addTags(persistableImagesTags);

		// then
		var tags1 = tagRepository.getTags(imageId1);
		assertThat(tags1).hasSize(1);
		assertThat(tags1).contains(new Tag("Some category", "Some tag"));

		var tags2 = tagRepository.getTags(imageId2);
		assertThat(tags2).hasSize(1);
		assertThat(tags2).contains(new Tag("Some category", "Some tag"));

		var tags3 = tagRepository.getTags(imageId3);
		assertThat(tags3).hasSize(1);
		assertThat(tags3).contains(new Tag("Some category", "Some tag"));

		var tags4 = tagRepository.getTags(imageId4);
		assertThat(tags4).hasSize(1);
		assertThat(tags4).contains(new Tag("Some category", "Some tag"));

		var tags5 = tagRepository.getTags(imageId5);
		assertThat(tags5).hasSize(1);
		assertThat(tags5).contains(new Tag("Some category", "Some tag"));

		var tags6 = tagRepository.getTags(imageId6);
		assertThat(tags6).hasSize(1);
		assertThat(tags6).contains(new Tag("Some category", "Some tag"));
	}
}
