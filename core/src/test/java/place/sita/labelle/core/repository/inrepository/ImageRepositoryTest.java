package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.image.ImageRepository;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.jooq.Tables;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageRepositoryTest extends TestContainersTest {

	@Autowired
	private ImageRepository imageRepository;


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
	public void shouldEmptyRepositoryReportZeroElements() {
		// when
		int count = imageRepository.images().count();

		// then
		assertThat(count).isEqualTo(0);
	}

	@Test
	public void shouldEmptyRepositoryHaveIteratorWithNoElements() {
		// when
		int countOfElements = 0;
		try (var iterator = imageRepository.images().getIterator()) {
			while (iterator.hasNext()) {
				countOfElements++;
				iterator.next();
			}
		}

		// then
		assertThat(countOfElements).isEqualTo(0);
	}

	@Test
	public void shouldEmptyRepositoryReportZeroElementsWhenFiltering() {
		// when
		int count = imageRepository.images().process().filterByImageId(UUID.randomUUID()).count();

		// then
		assertThat(count).isEqualTo(0);
	}

	@Test
	public void shouldEmptyRepositoryHaveIteratorWithNoElementsWhenFiltering() {
		// when
		int countOfElements = 0;
		try (var iterator = imageRepository.images().process().filterByImageId(UUID.randomUUID()).getIterator()) {
			while (iterator.hasNext()) {
				countOfElements++;
				iterator.next();
			}
		}

		// then
		assertThat(countOfElements).isEqualTo(0);
	}

	@Test
	public void shouldReportOneElementWhenAddingOne() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		int count = imageRepository.images().count();

		// then
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldReportOneElementWhenAddingOneAndFiltering() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		int count = imageRepository.images().process().filterByImageId(imageId).count();

		// then
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldReportOneElementWhenAddingOneAndFilteringByRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.addEmptySyntheticImage(repo.id());

		// when
		int count = imageRepository.images().process().filterByRepository(repo.id()).count();

		// then
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldReportHundredElementsWhenAddingHundred() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		for (int i = 0; i < 100; i++) {
			inRepositoryService.addEmptySyntheticImage(repo.id());
		}

		// when
		int count = imageRepository.images().count();

		// then
		assertThat(count).isEqualTo(100);
	}

	@Test
	public void shouldIterateOverHundredElements() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		Set<UUID> idsCreated = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			idsCreated.add(inRepositoryService.addEmptySyntheticImage(repo.id()));
		}

		// when
		Set<UUID> idsEncountered = new HashSet<>();
		try (var iterator = imageRepository.images().getIterator()) {
			while (iterator.hasNext()) {
				var image = iterator.next();
				idsEncountered.add(image.id());
			}
		}

		// then
		assertThat(idsEncountered).isEqualTo(idsCreated);
	}

	@Test
	public void shouldGetAllHundredElements() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		Set<UUID> idsCreated = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			idsCreated.add(inRepositoryService.addEmptySyntheticImage(repo.id()));
		}

		// when
		Set<UUID> ids = imageRepository.images().getAll().stream().map(ImageResponse::id).collect(Collectors.toSet());

		// then
		assertThat(ids).isEqualTo(idsCreated);
	}

	@Test
	public void shouldReportZeroElementsWhenFiltersAreExclusive() {
		// given
		Repository firstRepo = repositoryService.addRepository("Test repo");
		inRepositoryService.addEmptySyntheticImage(firstRepo.id());
		Repository secondRepo = repositoryService.addRepository("Test repo 2");
		inRepositoryService.addEmptySyntheticImage(secondRepo.id());

		// when
		int count = imageRepository.images().process().filterByRepository(firstRepo.id()).process().filterByRepository(secondRepo.id()).count();

		// then
		assertThat(count).isEqualTo(0);
	}

	@Test
	public void shouldFilterCorrectly() {
		// given
		Repository firstRepo = repositoryService.addRepository("Test repo");
		Set<UUID> imagesInFirstRepo = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			imagesInFirstRepo.add(inRepositoryService.addEmptySyntheticImage(firstRepo.id()));
		}
		Repository secondRepo = repositoryService.addRepository("Test repo 2");
		inRepositoryService.addEmptySyntheticImage(secondRepo.id());

		// when
		Set<UUID> images = imageRepository.images().process().filterByRepository(firstRepo.id()).getAll().stream().map(ImageResponse::id).collect(Collectors.toSet());

		// then
		assertThat(images).isEqualTo(imagesInFirstRepo);
	}

	@Test
	public void shouldPagingWorkCorrectly_1() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		for (int i = 0; i < 100; i++) {
			inRepositoryService.addEmptySyntheticImage(repo.id());
		}

		// when
		Set<UUID> images = imageRepository.images().getPage(new Page(0, 10)).getAll().stream().map(ImageResponse::id).collect(Collectors.toSet());

		// then
		assertThat(images).hasSize(10);
	}
}
