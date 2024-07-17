package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.UnexpectedExpressionException;
import place.sita.labelle.core.repository.inrepository.tags.Tag;

public class InMemoryTagContainerInvokee_ContainerLevel_Test {

	@Test
	public void shouldConditionallyRemoveTagSimpleCase() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") THEN IN tags DO (REMOVE (\"cat1\", \"tag1\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagIfTwoTagsExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") AND IN tags EXISTS (tag = \"tag2\") THEN IN tags DO (REMOVE (\"cat1\", \"tag1\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldNotConditionallyRemoveTagIfOneOfTwoTagsDontExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") AND IN tags EXISTS (tag = \"tag2\") THEN IN tags DO (REMOVE (\"cat1\", \"tag1\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldNotConditionallyRemoveTagIfBothOfTwoTagsDontExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") AND IN tags EXISTS (tag = \"tag2\") THEN IN tags DO (REMOVE (\"cat1\", \"tag1\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyRemoveTagBeANoopWhenTheActualTagDoesNotExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") THEN IN tags DO (REMOVE (\"cat1\", \"tag4\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3")
		);
	}

	@Test
	public void shouldConditionallyAddTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag1\") THEN IN tags DO (ADD (\"cat1\", \"tag4\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3"),
			new Tag("cat1", "tag4")
		);
	}

	@Test
	public void shouldConditionallyAddTagBeANoopWhenTheActualTagDoesNotExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));
		tags.add(new Tag("cat1", "tag3"));
		tags.add(new Tag("cat1", "tag4"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (ADD (\"cat1\", \"tag4\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2"),
			new Tag("cat1", "tag3"),
			new Tag("cat1", "tag4")
		);
	}

	@Test
	public void shouldNotConditionallyAddATagWhenTheActualTagDoesNotExist() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		Set<Tag> tags = new LinkedHashSet<>();
		tags.add(new Tag("cat1", "tag1"));
		tags.add(new Tag("cat1", "tag2"));

		// when
		invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (ADD (\"cat1\", \"tag4\"))");
		Set<Tag> results = invokee.applyToInvokee(tags);

		// then
		assertThat(results).containsExactly(
			new Tag("cat1", "tag1"),
			new Tag("cat1", "tag2")
		);
	}

	@Test
	public void shouldThrowOnAddingMatchedCategoryTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (ADD (MATCHED, \"tag4\"))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

	@Test
	public void shouldThrowOnAddingMatchedTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (ADD (\"cat1\", MATCHED))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

	@Test
	public void shouldThrowOnAddingMatchedCategoryAndTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (ADD (MATCHED, MATCHED))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

	@Test
	public void shouldThrowOnRemovingMatchedCategoryTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (REMOVE (MATCHED, \"tag4\"))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

	@Test
	public void shouldThrowOnRemovinggMatchedTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (REMOVE (\"cat1\", MATCHED))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

	@Test
	public void shouldThrowOnRemovingMatchedCategoryAndTag() {
		// given
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();

		// when / then
		assertThatThrownBy(() -> invokee.applyInstructions("IF IN tags EXISTS (tag = \"tag3\") THEN IN tags DO (REMOVE (MATCHED, MATCHED))"))
				.isInstanceOf(UnexpectedExpressionException.class);
	}

}
