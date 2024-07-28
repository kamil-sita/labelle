package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageServiceIdsTest extends TestContainersTest {

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
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("First category", "First tag"));
		inRepositoryService.addTag(imageId, new Tag("Second category", "Second tag"));
		// todo test deltas

		// when
		var duplicateId = inRepositoryService.images().duplicateImage(imageId);

		// then
		assertThat(inRepositoryService.getTags(duplicateId)).contains(new Tag("First category", "First tag"));
		assertThat(inRepositoryService.getTags(duplicateId)).contains(new Tag("Second category", "Second tag"));
		assertThat(inRepositoryService.getTags(duplicateId)).hasSize(2);
		// todo test persistent IDs
	}

	@Test
	public void shouldChangeIdsOfAnImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var result = inRepositoryService.images().updateIds(imageId, "newPersistentId", "newParentPersistentId", true);

		// then
		assertThat(result).isInstanceOf(ImageService.UpdateIdsResult.Success.class);
		var ids = inRepositoryService.images().getIds(imageId);
		assertThat(ids.persistentId()).isEqualTo("newPersistentId");
		assertThat(ids.parentPersistentId()).isEqualTo("newParentPersistentId");
		assertThat(ids.visibleToChildren()).isTrue();
	}

	@Test
	public void shouldNotChangeIdsOfAnImageIfItDuplicatesSomeOtherIdInThisRepo() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		var firstImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		var secondImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.images().updateIds(firstImageId, "newPersistentId", "newParentPersistentId", true);

		// when
		var result = inRepositoryService.images().updateIds(secondImageId, "newPersistentId", "something", true);

		// then
		assertThat(result).isInstanceOf(ImageService.UpdateIdsResult.IdReuse.class);
		var ids = inRepositoryService.images().getIds(firstImageId);
		assertThat(ids.persistentId()).isEqualTo("newPersistentId");
		assertThat(ids.parentPersistentId()).isEqualTo("newParentPersistentId");
		assertThat(ids.visibleToChildren()).isTrue();

		var ids2 = inRepositoryService.images().getIds(secondImageId);
		assertThat(ids2.persistentId()).isNotEqualTo("newPersistentId");
		assertThat(ids2.parentPersistentId()).isNotEqualTo("something");
		assertThat(ids2.visibleToChildren()).isTrue();
	}

	@Test
	public void shouldChangeIdsOfAnImageIfItDuplicatesSomeOtherIdInOtherRepo() {
		// given
		Repository firstRepo = repositoryService.addRepository("Test repo");
		Repository secondRepo = repositoryService.addRepository("Test repo 2");
		var firstImageId = inRepositoryService.images().addEmptySyntheticImage(firstRepo.id());
		var secondImageId = inRepositoryService.images().addEmptySyntheticImage(secondRepo.id());
		inRepositoryService.images().updateIds(firstImageId, "newPersistentId", "newParentPersistentId", true);

		// when
		var result = inRepositoryService.images().updateIds(secondImageId, "newPersistentId", "something", true);

		// then
		assertThat(result).isInstanceOf(ImageService.UpdateIdsResult.Success.class);
		var ids = inRepositoryService.images().getIds(firstImageId);
		assertThat(ids.persistentId()).isEqualTo("newPersistentId");
		assertThat(ids.parentPersistentId()).isEqualTo("newParentPersistentId");
		assertThat(ids.visibleToChildren()).isTrue();
	}

	@Test
	public void shouldChangeIdsOfAnImageKeepingPersistentId() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.images().updateIds(imageId, "newPersistentId", "newParentPersistentId", true);

		// when
		var result = inRepositoryService.images().updateIds(imageId, "newPersistentId", "something", true);

		// then
		assertThat(result).isInstanceOf(ImageService.UpdateIdsResult.Success.class);
		var ids = inRepositoryService.images().getIds(imageId);
		assertThat(ids.persistentId()).isEqualTo("newPersistentId");
		assertThat(ids.parentPersistentId()).isEqualTo("something");
		assertThat(ids.visibleToChildren()).isTrue();
	}

}
