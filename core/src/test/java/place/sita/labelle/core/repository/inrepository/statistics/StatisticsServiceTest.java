package place.sita.labelle.core.repository.inrepository.statistics;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsServiceTest extends TestContainersTest {

	@Autowired
	private StatisticsService statisticsService;

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
	public void shouldReturnNothingWhenNoTags() {
		// given
		var repository = repositoryService.addRepository("test");

		// when
		List<TagWithCountResponse> tagCount = statisticsService.getTagCount(repository.id());

		// then
		assertThat(tagCount).isEmpty();
	}

	@Test
	public void shouldReturnTagCountForSingleTag() {
		// given
		var repository = repositoryService.addRepository("test");
		var image = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(image, null, "category", "tag");

		// when
		List<TagWithCountResponse> tagCount = statisticsService.getTagCount(repository.id());

		// then
		assertThat(tagCount).hasSize(1);
		assertThat(tagCount.get(0).tag().tag()).isEqualTo("tag");
		assertThat(tagCount.get(0).tag().category()).isEqualTo("category");
		assertThat(tagCount.get(0).count()).isEqualTo(1);
	}

	@Test
	public void shouldReturnTagCountForMultipleTags() {
		// given
		var repository = repositoryService.addRepository("test");
		var image = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(image, null, "category", "tag");
		inRepositoryService.addTag(image, null, "category", "tag2");
		var image2 = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(image2, null, "category", "tag");

		// when
		List<TagWithCountResponse> tagCount = statisticsService.getTagCount(repository.id());

		// then
		assertThat(tagCount).hasSize(2);
		assertThat(tagCount.get(0).tag().tag()).isEqualTo("tag");
		assertThat(tagCount.get(0).tag().category()).isEqualTo("category");
		assertThat(tagCount.get(0).count()).isEqualTo(2);
		assertThat(tagCount.get(1).tag().tag()).isEqualTo("tag2");
		assertThat(tagCount.get(1).tag().category()).isEqualTo("category");
		assertThat(tagCount.get(1).count()).isEqualTo(1);
	}

	@Test
	public void shouldNotFindTagsFromDifferentRepository() {
		// given
		var repository = repositoryService.addRepository("test");
		var repository2 = repositoryService.addRepository("test2");
		var image = inRepositoryService.addEmptySyntheticImage(repository.id());
		inRepositoryService.addTag(image, null, "category", "tag");

		// when
		List<TagWithCountResponse> tagCount = statisticsService.getTagCount(repository2.id());

		// then
		assertThat(tagCount).isEmpty();
	}

}
