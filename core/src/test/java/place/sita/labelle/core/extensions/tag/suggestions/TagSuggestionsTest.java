package place.sita.labelle.core.extensions.tag.suggestions;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.extensions.tag.suggestions.TagSuggestions;
import place.sita.labelle.jooq.Tables;

import static org.assertj.core.api.Assertions.assertThat;

public class TagSuggestionsTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private CacheRegistry cacheRegistry;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private TagSuggestions tagSuggestions;

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
	public void shouldReturnNoSuggestionsWhenThereAreNoCategories() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).isEmpty();
	}

	@Test
	public void shouldReturnNoSuggestionsWhenThereAreNoTags() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).isEmpty();
	}

	@Test
	public void shouldReturnNoTagSuggestionWhenCurrentImageIsTheOnlyOneWithTag() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).isEmpty();
	}

	@Test
	public void shouldReturnNoCategorySuggestionWhenCurrentImageIsTheOnlyOneWithTag() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).isEmpty();
	}

	@Test
	public void shouldReturnTagSuggestionWhenTagExists() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY", "TAG2"));

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).containsExactly("TAG2");
	}

	@Test
	public void shouldReturnCategorySuggestionWhenCategoryExists() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY2", "TAG"));

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).containsExactly("CATEGORY2");
	}

	@Test
	public void shouldReturnMultipleTagSuggestions() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY", "TAG2"));
		var anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("CATEGORY", "TAG3"));

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).containsExactly("TAG2", "TAG3");
	}

	@Test
	public void shouldReturnMultipleCategorySuggestions() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY2", "TAG"));
		var anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("CATEGORY3", "TAG"));

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("", "", imageId);

		// then
		assertThat(suggestions).containsExactly("CATEGORY2", "CATEGORY3");
	}

	@Test
	public void shouldReturnFilteredTagSuggestions() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY", "TAG2"));
		var anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("CATEGORY", "TAG3"));
		inRepositoryService.addTag(anotherImageId, new Tag("CATEGORY", "TAG3"));

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("CAT", "2", imageId);

		// then
		assertThat(suggestions).containsExactly("TAG2");
	}

	@Test
	public void shouldReturnFilteredCategorySuggestions() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY2", "TAG"));
		var anotherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(anotherImageId, new Tag("CATEGORY3", "TAG"));
		inRepositoryService.addTag(anotherImageId, new Tag("NOTHING2", "something"));

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("2", "TAG", imageId);

		// then
		assertThat(suggestions).containsExactly("CATEGORY2");
	}

	@Test
	public void shouldSuggestTagsIgnoringCase() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("CATEGORY", "SOMETHING"));

		// when
		var suggestions = tagSuggestions.getTagSuggestionsForImage("", "some", imageId);

		// then
		assertThat(suggestions).containsExactly("SOMETHING");
	}

	@Test
	public void shouldSuggestCategoriesIgnoringCase() {
		// given
		var repo = repositoryService.addRepository("Repo");
		var imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(imageId, new Tag("CATEGORY", "TAG"));
		var otherImageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.addTag(otherImageId, new Tag("SOMETHING", "TAG"));

		// when
		var suggestions = tagSuggestions.getCategorySuggestionsForImage("some", "", imageId);

		// then
		assertThat(suggestions).containsExactly("SOMETHING");
	}
}
