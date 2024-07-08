package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import org.junit.jupiter.api.Test;
import place.sita.labelle.core.repository.inrepository.tags.TagImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class StringToTagFilterTest {

	@Test
	public void shouldAnyAcceptAllFilters() {
		// given
		String query = "ANY";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("tag1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag2", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag3", "tag3"))).isTrue();
	}

	@Test
	public void shouldNotAnyRejectAllFilters() {
		// given
		String query = "NOT ANY";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("tag1", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag3", "tag3"))).isFalse();
	}

	@Test
	public void shouldMatchByCategory() {
		// given
		String query = "category = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("tag1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag2", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag3", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByTag() {
		// given
		String query = "tag = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("tag1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag2", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag3", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("tag1", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("tag3", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByCategoryAndTag() {
		// given
		String query = "category = \"cat1\" AND tag = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByCategoryOrTag() {
		// given
		String query = "category = \"cat1\" OR tag = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByCategoryAndNotTag() {
		// given
		String query = "category = \"cat1\" AND NOT tag = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByCategoryOrNotTag() {
		// given
		String query = "category = \"cat1\" OR NOT tag = \"tag1\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isTrue();
	}

	@Test
	public void shouldMatchByLikeCategory() {
		// given
		String query = "category like \"category.*\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("category1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("category2", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("category3", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isFalse();
	}

	@Test
	public void shouldMatchByLikeTag() {
		// given
		String query = "tag like \"tag.*\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag3"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "something"))).isFalse();
	}

	@Test
	public void shouldMatchByAndLike() {
		// given
		String query = "category like \"cat.*\" AND tag like \"tag.*\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag3"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "something"))).isFalse();
		assertThat(filter.filter(new TagImpl("something", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("something", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("something", "tag3"))).isFalse();
		assertThat(filter.filter(new TagImpl("something", "something"))).isFalse();
	}

	@Test
	public void shouldMatchByCategoryIn() {
		// given
		String query = "category in (\"cat1\", \"cat2\")";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
	}

	@Test
	public void shouldMatchByTagIn() {
		// given
		String query = "tag in (\"tag1\", \"tag2\")";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag3"))).isFalse();
	}

	@Test
	public void shouldMatchByTupleIn() {
		// given
		String query = "(category, tag) in ((\"cat1\", \"tag1\"), (\"cat2\", \"tag2\"))";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByOtherTupleIn() {
		// given
		String query = "(tag, category) in ((\"tag1\", \"cat1\"), (\"tag2\", \"cat2\"))";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByOneDimensionTupleIn() {
		// given
		String query = "(tag) in ((\"tag1\"), (\"tag2\"))";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat1", "tag3"))).isFalse();
	}

	@Test
	public void shouldNotMatchWhenTagsAreExclusive() {
		// given
		String query = "tag = \"tag1\" AND tag = \"tag2\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}

	@Test
	public void shouldNotMatchWhenCategoriesAreExclusive() {
		// given
		String query = "category = \"cat1\" AND category = \"cat2\"";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByExactTag() {
		// given
		String query = "(tag, category) = (\"tag1\", \"cat1\")";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}

	@Test
	public void shouldMatchByExactTagReversed() {
		// given
		String query = "(category, tag) = (\"cat1\", \"tag1\")";

		// when
		TagFilter filter = StringToTagFilter.fromString(query);

		// then
		assertThat(filter.filter(new TagImpl("cat1", "tag1"))).isTrue();
		assertThat(filter.filter(new TagImpl("cat2", "tag2"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat3", "tag1"))).isFalse();
		assertThat(filter.filter(new TagImpl("cat1", "tag2"))).isFalse();
	}
}
