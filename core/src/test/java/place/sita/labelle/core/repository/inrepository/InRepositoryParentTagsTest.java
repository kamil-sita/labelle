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

		var parentRepo1Image = inRepositoryService.addEmptySyntheticImage(parentRepo1.id());
		inRepositoryService.setPersistentId(parentRepo1Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo1Image, null, "CATEGORY1", "TAG1");
		inRepositoryService.addTag(parentRepo1Image, null, "CATEGORY2", "TAG2");
		var parentRepo2Image = inRepositoryService.addEmptySyntheticImage(parentRepo2.id());
		inRepositoryService.setPersistentId(parentRepo2Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo2Image, null, "CATEGORY3", "TAG3");
		inRepositoryService.addTag(parentRepo2Image, null, "CATEGORY1", "TAG1");
		var parentRepo3Image = inRepositoryService.addEmptySyntheticImage(parentRepo3.id());
		inRepositoryService.setPersistentId(parentRepo3Image, "PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo3Image, null, "CATEGORY4", "TAG4");
		var unrelatedRepoImage = inRepositoryService.addEmptySyntheticImage(unrelatedRepo.id());
		inRepositoryService.setPersistentId(unrelatedRepoImage, "PERSISTENT_ID");
		inRepositoryService.addTag(unrelatedRepoImage, null, "CATEGORY5", "TAG5");

		var parentRepo2UnrelatedImage = inRepositoryService.addEmptySyntheticImage(parentRepo2.id());
		inRepositoryService.setPersistentId(parentRepo2UnrelatedImage, "UNRELATED_PERSISTENT_ID");
		inRepositoryService.addTag(parentRepo2UnrelatedImage, null, "CATEGORY6", "TAG6");

		var childRepoImage = inRepositoryService.addEmptySyntheticImage(childRepo.id());
		inRepositoryService.setParentPersistentId(childRepoImage, "PERSISTENT_ID");
		inRepositoryService.setPersistentId(childRepoImage, "CHILD_PERSISTENT_ID");
		inRepositoryService.addTag(childRepoImage, null, "CATEGORY7", "TAG7");

		// when
		Set<Tag> parentTags = inRepositoryService.parentTags(childRepoImage);

		// then
		assertThat(parentTags).containsExactlyInAnyOrder(
				new Tag("TAG1", "CATEGORY1"),
				new Tag("TAG2", "CATEGORY2"),
				new Tag("TAG3", "CATEGORY3"),
				new Tag("TAG4", "CATEGORY4")
		);
	}

}
