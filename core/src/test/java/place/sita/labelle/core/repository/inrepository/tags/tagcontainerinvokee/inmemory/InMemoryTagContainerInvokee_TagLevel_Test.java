package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import org.junit.jupiter.api.Test;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
import place.sita.labelle.core.repository.inrepository.tags.Tag;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class InMemoryTagContainerInvokee_TagLevel_Test {

	@Test
	public void shouldConditionallyRemoveTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF tag = \"tag1\" THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagBeANoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF tag = \"tag1\" THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveADifferentTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF tag = \"tag2\" THEN REMOVE (\"cat1\", \"tag1\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveADifferentTagBeANoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF tag = \"tag2\" THEN REMOVE (\"cat1\", \"tag1\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagsByMatchingCategory() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));
		tags.add(new Tag("cat2", "tag4"));

		// when
		invokee.applyInstructions("IF tag = \"tag2\" THEN REMOVE (MATCHED, \"tag1\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag3"),
			new Tag("cat2", "tag4")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagsByMatchingCategoryDual() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));
		tags.add(new Tag("cat2", "tag4"));
		tags.add(new Tag("cat3", "tag5"));
		tags.add(new Tag("cat3", "tag1"));

		// when
		invokee.applyInstructions("IF tag = \"tag2\" OR tag = \"tag5\" THEN REMOVE (MATCHED, \"tag1\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag3"),
			new Tag("cat2", "tag4"),
			new Tag("cat3", "tag5")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagsByMatchingTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag3"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));
		tags.add(new Tag("cat2", "tag4"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN REMOVE (\"cat1\", MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag3"),
			new Tag("cat2", "tag4")
		);
	}



	@Test
	public void shouldConditionallyRemoveTagsByMatchingBoth() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag3"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));
		tags.add(new Tag("cat2", "tag4"));
		tags.add(new Tag("cat3", "tag3"));
		tags.add(new Tag("cat3", "tag4"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" AND tag = \"tag3\" THEN REMOVE (MATCHED, MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag3"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag4"),
			new Tag("cat3", "tag3"),
			new Tag("cat3", "tag4")
		);
	}

	@Test
	public void shouldUnconditionallyJustRemove() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).isEmpty();
	}

	@Test
	public void shouldUnconditionallyJustRemoveBeANoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();

		// when
		invokee.applyInstructions("REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).isEmpty();
	}

	@Test
	public void shouldConditionallyAdd() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN ADD (\"cat1\", \"tag4\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag3"),
			new Tag("cat1", "tag4")
		);
	}

	@Test
	public void shouldConditionallyAddBeANoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag3"));
		tags.add(new Tag("cat1", "tag4"));

		// when
		invokee.applyInstructions("IF category = \"cat1\" THEN ADD (\"cat1\", \"tag4\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag3"),
			new Tag("cat1", "tag4")
		);
	}

	@Test
	public void shouldConditionallyAddNotMatch() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));
		tags.add(new Tag("cat3", "tag3"));
		tags.add(new Tag("cat3", "tag4"));

		// when
		invokee.applyInstructions("IF category = \"cat1\" THEN ADD (\"cat1\", \"tag4\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2"),
			new Tag("cat3", "tag3"),
			new Tag("cat3", "tag4")
		);
	}

	@Test
	public void shouldConditionallyCopyCategory() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF category = \"cat1\" THEN ADD (MATCHED, \"something\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("cat1", "something")
		);
	}

	@Test
	public void shouldConditionallyCopyCategoryBeANoopDueToExistence() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag2"));
		tags.add(new Tag("cat3", "something"));

		// when
		invokee.applyInstructions("IF category = \"cat3\" THEN ADD (MATCHED, \"something\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("cat3", "tag2"),
			new Tag("cat3", "something")
		);
	}

	@Test
	public void shouldConditionallyCopyCategoryBeANoopDueToNoMatch() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF category = \"cat3\" THEN ADD (MATCHED, \"something\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldConditionallyCopyTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "xyz"));
		tags.add(new Tag("cat2", "abc"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN ADD (\"something\", MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "xyz"),
			new Tag("cat2", "abc"),
			new Tag("something", "abc"),
			new Tag("something", "xyz")
		);
	}

	@Test
	public void shouldConditionallyCopyTagBeANoopDueToExistence() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "xyz"));
		tags.add(new Tag("cat2", "abc"));
		tags.add(new Tag("something", "abc"));
		tags.add(new Tag("something", "xyz"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN ADD (\"something\", MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "xyz"),
			new Tag("cat2", "abc"),
			new Tag("something", "abc"),
			new Tag("something", "xyz")
		);
	}

	@Test
	public void shouldConditionallyCopyTagBeANoopDueToNoMatch() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "xyz"));
		tags.add(new Tag("cat2", "abc"));

		// when
		invokee.applyInstructions("IF category = \"cat3\" THEN ADD (\"something\", MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "xyz"),
			new Tag("cat2", "abc")
		);
	}


	@Test
	public void shouldConditionallyCopyCategoryAndTagBeANoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "xyz"));
		tags.add(new Tag("cat2", "abc"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN ADD (MATCHED, MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "xyz"),
			new Tag("cat2", "abc")
		);
	}

	@Test
	public void shouldConditionallyCopyCategoryAndTagBeANoopDueToNoMatch() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "xyz"));
		tags.add(new Tag("cat2", "abc"));
		tags.add(new Tag("xyz", "xyz"));
		tags.add(new Tag("xyz", "abc"));

		// when
		invokee.applyInstructions("IF category = \"nothing\" THEN ADD (MATCHED, MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "xyz"),
			new Tag("cat2", "abc"),
			new Tag("xyz", "xyz"),
			new Tag("xyz", "abc")
		);
	}

	@Test
	public void shouldConditionallyReplaceTags() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF category = \"cat2\" THEN REPLACE WITH (\"censor\", \"censored\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("censor", "censored")
		);
	}

	@Test
	public void shouldReplaceTagsWithSameTagBeNoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF (category = \"cat2\" AND tag=\"tag2\") THEN REPLACE WITH (\"cat2\", \"tag2\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldReplaceTagsWithMatchedBeNoop() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF (category = \"cat2\" AND tag=\"tag2\") THEN REPLACE WITH (MATCHED, MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldReplaceCategoryWithMatched() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF (category = \"cat2\" AND tag=\"tag2\") THEN REPLACE WITH (MATCHED, \"something\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "something")
		);
	}

	@Test
	public void shouldReplaceTagWithMatched() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF (category = \"cat2\" AND tag=\"tag2\") THEN REPLACE WITH (\"something\", MATCHED)");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("something", "tag2")
		);
	}

	@Test
	public void shouldEvaluateOrExpression() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF (category = \"cat2\" OR tag=\"tag2\") THEN ADD (\"something\", MATCHED), ADD (MATCHED, \"something\")");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("something", "tag2"),
			new Tag("something", "tag1"),
			new Tag("cat1", "something"),
			new Tag("cat2", "something")
		);
	}

	@Test
	public void shouldEvaluateNotExpression() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));

		// when
		invokee.applyInstructions("IF NOT (category = \"cat2\" OR tag=\"tag2\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldMatchByInTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat2", "tag3"));

		// when
		invokee.applyInstructions("IF tag in (\"tag1\", \"tag3\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldMatchByInCategory() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF category in (\"cat1\", \"cat3\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchCategoryByName() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF tag in (\"cat1\", \"cat3\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchTagByName() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF category in (\"tag1\", \"tag2\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchByCategoryTagEqualTuple() {
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF (category, tag) = (\"cat1\", \"tag1\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat2", "tag2"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchByTagCategoryEqualTuple() {
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF (tag, category) = (\"tag1\", \"cat2\") THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag2"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchByCategoryTagInTuple() {
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF (category, tag) in ((\"cat1\", \"tag1\"), (\"cat2\", \"tag2\")) THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldActuallyMatchByTagCategoryInTuple() {
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat2", "tag1"));
		tags.add(new Tag("cat2", "tag2"));
		tags.add(new Tag("cat3", "tag1"));
		tags.add(new Tag("cat3", "tag2"));

		// when
		invokee.applyInstructions("IF (tag, category) in ((\"tag1\", \"cat1\"), (\"tag2\", \"cat2\")) THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat2", "tag1"),
			new Tag("cat3", "tag1"),
			new Tag("cat3", "tag2")
		);
	}

	@Test
	public void shouldMatchByCategoryNameLike() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("abcde", "fghij"));
		tags.add(new Tag("bcdea", "ghijf"));
		tags.add(new Tag("cdeab", "hijfg"));
		tags.add(new Tag("deabc", "ijfgh"));
		tags.add(new Tag("eabcd", "jfghi"));

		// when
		invokee.applyInstructions("IF category like \".*bcde.*\" THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cdeab", "hijfg"),
			new Tag("deabc", "ijfgh"),
			new Tag("eabcd", "jfghi")
		);
	}

	@Test
	public void shouldMatchByTagNameLike() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("abcde", "fghij"));
		tags.add(new Tag("bcdea", "ghijf"));
		tags.add(new Tag("cdeab", "hijfg"));
		tags.add(new Tag("deabc", "ijfgh"));
		tags.add(new Tag("eabcd", "jfghi"));

		// when
		invokee.applyInstructions("IF tag like \".*fgh.*\" THEN REMOVE");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("bcdea", "ghijf"),
			new Tag("cdeab", "hijfg")
		);
	}


}
