package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InRepositoryParentTagsTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private CacheRegistry cacheRegistry;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private RepositoryService repositoryService;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_CATEGORY).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();

		context.delete(Tables.IMAGE_FILE).execute();
		context.delete(Tables.ROOT).execute();
		cacheRegistry.invalidate();
	}

	@Test
	public void shouldGetParentTags() {
		// given
		var childRepo = repositoryService.addRepository("Child repo");
		var parentRepo1 = repositoryService.addRepository("Parent repo 1");
		var parentRepo2 = repositoryService.addRepository("Parent repo 2");
		var parentRepo3 = repositoryService.addRepository("Parent repo 3");
		var unrelatedRepo = repositoryService.addRepository("Unrelated repo");

		repositoryService.addParentChild(childRepo.id(), parentRepo1.id());
		repositoryService.addParentChild(childRepo.id(), parentRepo2.id());
		repositoryService.addParentChild(childRepo.id(), parentRepo3.id());

		var parentRepo1Image = inRepositoryService.images().addEmptySyntheticImage(parentRepo1.id());
		inRepositoryService.images().setPersistentId(parentRepo1Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo1Image, new Tag("CATEGORY1", "TAG1"));
		inRepositoryService.addTag(parentRepo1Image, new Tag("CATEGORY2", "TAG2"));
		var parentRepo2Image = inRepositoryService.images().addEmptySyntheticImage(parentRepo2.id());
		inRepositoryService.images().setPersistentId(parentRepo2Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo2Image, new Tag("CATEGORY3", "TAG3"));
		inRepositoryService.addTag(parentRepo2Image, new Tag("CATEGORY1", "TAG1"));
		var parentRepo3Image = inRepositoryService.images().addEmptySyntheticImage(parentRepo3.id());
		inRepositoryService.images().setPersistentId(parentRepo3Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo3Image, new Tag("CATEGORY4", "TAG4"));
		var unrelatedRepoImage = inRepositoryService.images().addEmptySyntheticImage(unrelatedRepo.id());
		inRepositoryService.images().setPersistentId(unrelatedRepoImage, "PERSISTENT_ID");
		inRepositoryService.addTag(unrelatedRepoImage, new Tag("CATEGORY5", "TAG5"));

		var parentRepo2UnrelatedImage = inRepositoryService.images().addEmptySyntheticImage(parentRepo2.id());
		inRepositoryService.images().setPersistentId(parentRepo2UnrelatedImage, "UNRELATED_PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo2UnrelatedImage, new Tag("CATEGORY6", "TAG6"));

		var childRepoImage = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		inRepositoryService.images().setParentPersistentId(childRepoImage, "PERSISTENT_ID");
		inRepositoryService.images().setPersistentId(childRepoImage, "CHILD_PERSISTENT_ID");
		inRepositoryService.addTag(childRepoImage, new Tag("CATEGORY7", "TAG7"));

		// when
		Set<Tag> parentTags = inRepositoryService.parentTags(childRepoImage);

		// then
		assertThat(parentTags).containsExactlyInAnyOrder(
				new Tag("CATEGORY1", "TAG1"),
				new Tag("CATEGORY2", "TAG2"),
				new Tag("CATEGORY3", "TAG3"),
				new Tag("CATEGORY4", "TAG4")
		);
	}

}
