package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import static org.assertj.core.api.Assertions.assertThat;

public class InRepositoryTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private CacheRegistry cacheRegistry;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_DELTA).execute();
		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_CATEGORY).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY).execute();

		context.delete(Tables.IMAGE_FILE).execute();
		context.delete(Tables.ROOT).execute();
		cacheRegistry.invalidate();
	}

	@Test
	public void shouldCreateDuplicateOfImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		var imageId = inRepositoryService.addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, repo.id(), new Tag("First category", "First tag"));
		inRepositoryService.addTag(imageId, repo.id(), new Tag("Second category", "Second tag"));
		// todo test deltas

		// when
		var duplicateId = inRepositoryService.duplicateImage(imageId);

		// then
		assertThat(inRepositoryService.getTags(duplicateId)).contains(new Tag("First category", "First tag"));
		assertThat(inRepositoryService.getTags(duplicateId)).contains(new Tag("Second category", "Second tag"));
		assertThat(inRepositoryService.getTags(duplicateId)).hasSize(2);
		// todo test persistent IDs
	}


}
