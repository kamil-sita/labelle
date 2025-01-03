package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.images.imagelocator.ImageLocatorService;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.datasource.NonUniqueAnswerException;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.jooq.Tables;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ImageRepositoryTest extends TestContainersTest {

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private DSLContext context;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private ImageLocatorService imageLocatorService;

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
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		int count = imageRepository.images().count();

		// then
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldReportOneElementWhenAddingOneAndFiltering() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		int count = imageRepository.images().process().filterByImageId(imageId).count();

		// then
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldReportOneElementWhenAddingOneAndFilteringByRepository() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

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
			inRepositoryService.images().addEmptySyntheticImage(repo.id());
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
			idsCreated.add(inRepositoryService.images().addEmptySyntheticImage(repo.id()));
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
			idsCreated.add(inRepositoryService.images().addEmptySyntheticImage(repo.id()));
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
		inRepositoryService.images().addEmptySyntheticImage(firstRepo.id());
		Repository secondRepo = repositoryService.addRepository("Test repo 2");
		inRepositoryService.images().addEmptySyntheticImage(secondRepo.id());

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
			imagesInFirstRepo.add(inRepositoryService.images().addEmptySyntheticImage(firstRepo.id()));
		}
		Repository secondRepo = repositoryService.addRepository("Test repo 2");
		inRepositoryService.images().addEmptySyntheticImage(secondRepo.id());

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
			inRepositoryService.images().addEmptySyntheticImage(repo.id());
		}

		// when
		Set<UUID> images = imageRepository.images().getPage(new Page(0, 10)).getAll().stream().map(ImageResponse::id).collect(Collectors.toSet());

		// then
		assertThat(images).hasSize(10);
	}

	@Test
	public void shouldPagingWorkCorrectly_2() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		Set<UUID> imagesInRepo = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			imagesInRepo.add(inRepositoryService.images().addEmptySyntheticImage(repo.id()));
		}

		// when
		Set<UUID> foundImages = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			foundImages.addAll(
				imageRepository.images().getPage(new Page(10 * i, 10)).getAll().stream().map(ImageResponse::id).collect(Collectors.toSet())
			);
		}

		// then
		assertThat(foundImages).isEqualTo(imagesInRepo);
	}

	@Test
	public void shouldPagingWorkCorrectly_3() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		for (int i = 0; i < 15; i++) {
			inRepositoryService.images().addEmptySyntheticImage(repo.id());
		}

		// when
		int count = imageRepository.images().getPage(new Page(10, 10)).count();

		// then
		assertThat(count).isEqualTo(5);
	}

	@Test
	public void shouldRemoveImages() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		for (int i = 0; i < 100; i++) {
			inRepositoryService.images().addEmptySyntheticImage(repo.id());
		}

		// when
		imageRepository.images().remove();

		// then
		assertThat(imageRepository.images().count()).isEqualTo(0);
	}

	@Test
	public void shouldRemoveSomeImages() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		Set<UUID> extraImagesInRepo = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			extraImagesInRepo.add(inRepositoryService.images().addEmptySyntheticImage(repo.id()));
		}

		// when
		while (!extraImagesInRepo.isEmpty()) {
			UUID any = extraImagesInRepo.iterator().next();
			imageRepository.images().process().filterByImageId(any).remove();
			extraImagesInRepo.remove(any);
		}

		// then
		assertThat(imageRepository.images().count()).isEqualTo(1);
		assertThat(imageRepository.images().getAll().stream().map(ImageResponse::id).collect(Collectors.toSet())).containsExactly(image);
	}

	@Test
	public void shouldGetAnyImageOptional_isAvailable() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var optional = imageRepository.images().getAnyOptional();

		// then
		assertThat(optional).isPresent();
		assertThat(optional.get().id()).isEqualTo(image);
	}

	@Test
	public void shouldGetAnyImageOptional_isAvailable_multipleImages() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID image2 = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var optional = imageRepository.images().getAnyOptional();

		// then
		assertThat(optional).isPresent();
		HashSet<UUID> uniqueIds = new HashSet<>();
		uniqueIds.add(optional.get().id());
		uniqueIds.add(image);
		uniqueIds.add(image2);
		assertThat(uniqueIds).hasSize(2);
	}

	@Test
	public void shouldNotGetAnyImageOptional_becauseOfFiltering() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var optional = imageRepository.images().process().filterByImageId(UUID.randomUUID()).getAnyOptional();

		// then
		assertThat(optional).isEmpty();
	}

	@Test
	public void shouldGetAnyImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var any = imageRepository.images().getAny();

		// then
		assertThat(any.id()).isEqualTo(image);
	}

	@Test
	public void shouldGetAnyImage_multipleImages() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID image2 = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var any = imageRepository.images().getAny();

		// then
		HashSet<UUID> uniqueIds = new HashSet<>();
		uniqueIds.add(any.id());
		uniqueIds.add(image);
		uniqueIds.add(image2);
		assertThat(uniqueIds).hasSize(2);
	}

	@Test
	public void shouldNotGetAnyImage() {
		// when
		ImageResponse any = imageRepository.images().getAny();

		// then
		assertThat(any).isNull();
	}

	@Test
	public void shouldGetOnlyImage() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var only = imageRepository.images().getOne();

		// then
		assertThat(only.id()).isEqualTo(image);
	}

	@Test
	public void shouldGetOnlyImageOptional() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID image = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var optional = imageRepository.images().getOneOptional();

		// then
		assertThat(optional).isPresent();
		assertThat(optional.get().id()).isEqualTo(image);
	}

	@Test
	public void shouldNotGetOnlyImageOptional() {
		// when
		var optional = imageRepository.images().getOneOptional();

		// then
		assertThat(optional).isEmpty();
	}

	@Test
	public void shouldThrowWhenThereAreMultipleImagesAndRequiringOnly() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when / then
		assertThatThrownBy(() -> imageRepository.images().getOne())
			.isInstanceOf(NonUniqueAnswerException.class);
	}

	@Test
	public void shouldReturnCorrectIndexOf() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/some_basic_directory/");
		imageLocatorService.createRoot("C:/some_other_directory/");

		// when / then
		ImageResponse fifthImage = inRepositoryService.images().addImage(repo.id(), "C:/some_other_directory/image2.jpg").getSuccess();
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/some_other_directory/image1.jpg").getSuccess();
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/some_basic_directory/image3.jpg").getSuccess();
		ImageResponse sixthImage = inRepositoryService.images().addImage(repo.id(), "C:/some_other_directory/image3.jpg").getSuccess();
		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/some_basic_directory/image1.jpg").getSuccess();
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/some_basic_directory/image2.jpg").getSuccess();

		assertThat(imageRepository.images().indexOf(firstImage)).isEqualTo(0);
		assertThat(imageRepository.images().indexOf(secondImage)).isEqualTo(1);
		assertThat(imageRepository.images().indexOf(thirdImage)).isEqualTo(2);
		assertThat(imageRepository.images().indexOf(fourthImage)).isEqualTo(3);
		assertThat(imageRepository.images().indexOf(fifthImage)).isEqualTo(4);
		assertThat(imageRepository.images().indexOf(sixthImage)).isEqualTo(5);
	}

	@Test
	public void shouldFilterByEqualPathUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();

		// when
		var images = imageRepository.images().process().filterUsingTfLang("path = \"C:/dir_2/image1.jpg\"").getAll();

		// then
		assertThat(images).containsExactly(thirdImage);
	}

	@Test
	public void shouldFilterByPathLikeUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();

		// when
		var images = imageRepository.images().process().filterUsingTfLang("path like \"%dir_2%\"").getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(thirdImage, fourthImage);
	}

	@Test
	public void shouldFilterByCategoryOfTagInImageUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang("IN tags EXISTS (category = \"category2\")").getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(secondImage, fourthImage);
	}

	@Test
	public void shouldFilterByTagInImageUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang("IN tags EXISTS (tag = \"tag1\")").getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(firstImage, fourthImage);
	}

	@Test
	public void shouldFilterByCategoryTagTupleOfImageUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang("IN tags EXISTS ((category, tag) = (\"category1\", \"tag1\"))").getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(firstImage);
	}

	@Test
	public void shouldFilterByCategoryInOfImageUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category3", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category3", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category4", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category4", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang("IN tags EXISTS (category in (\"category2\", \"category4\"))").getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(secondImage, fourthImage);
	}

	@Test
	public void shouldFilterByInTupleOfImageUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				IN tags EXISTS (
					(category, tag) in (
						("category1", "tag1"),
						("category2", "tag2")
					)
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(firstImage, fourthImage);
	}

	@Test
	public void shouldFilterUsingNotStatementUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				NOT (
					IN tags EXISTS (
						(category, tag) in (
							("category1", "tag1"),
							("category2", "tag2")
						)
					)
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(secondImage, thirdImage);
	}

	@Test
	public void shouldFilterUsingAndStatementUsingTfLang_1() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				IN tags EXISTS (
					(category, tag) in (
						("category1", "tag1"),
						("category2", "tag2")
					)
				)
				AND
				IN tags EXISTS (
					(category, tag) in (
						("category2", "tag2"),
						("category1", "tag4")
					)
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(fourthImage);
	}

	@Test
	public void shouldFilterUsingOrStatementUsingTfLang() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				IN tags EXISTS (
					(category, tag) in (
						("category1", "tag1"),
						("category2", "tag2")
					)
				)
				OR
				IN tags EXISTS (
					(category, tag) in (
						("category2", "tag2"),
						("category1", "tag4")
					)
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(firstImage, thirdImage, fourthImage);
	}

	@Test
	public void shouldFilterUsingOrStatementUsingTfLangOptimizedCase() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag5"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				IN tags EXISTS (
					tag = "tag1"
				)
				OR
				IN tags EXISTS (
					tag = "tag3"
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(firstImage, secondImage, fourthImage);
	}

	@Test
	public void shouldFilterUsingAndStatementUsingTfLang_2() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		imageLocatorService.createRoot("C:/dir_1/");
		imageLocatorService.createRoot("C:/dir_2/");

		ImageResponse firstImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image1.jpg").getSuccess();
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag1"));
		inRepositoryService.addTag(firstImage.id(), new Tag("category1", "tag2"));
		ImageResponse secondImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_1/image2.jpg").getSuccess();
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag3"));
		inRepositoryService.addTag(secondImage.id(), new Tag("category2", "tag4"));
		ImageResponse thirdImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image1.jpg").getSuccess();
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag3"));
		inRepositoryService.addTag(thirdImage.id(), new Tag("category1", "tag4"));
		ImageResponse fourthImage = inRepositoryService.images().addImage(repo.id(), "C:/dir_2/image2.jpg").getSuccess();
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag1"));
		inRepositoryService.addTag(fourthImage.id(), new Tag("category2", "tag2"));

		// when
		var images = imageRepository.images().process().filterUsingTfLang(
			"""
				IN tags EXISTS (
					(category, tag) in (
						("category1", "tag1"),
						("category2", "tag2")
					)
				)
				AND
				IN tags EXISTS (
					(category, tag) in (
						("category2", "tag1"),
						("category1", "tag4")
					)
				)
				"""
		).getAll();

		// then
		assertThat(images).containsExactlyInAnyOrder(fourthImage);
	}
}
