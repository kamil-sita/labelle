package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory;

import org.apache.commons.lang3.NotImplementedException;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.TagContainerInvokee;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.TagStringOrMatched;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.TciInstruction;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.action.*;
import place.sita.labelle.core.repository.inrepository.tags.Tag;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class InMemoryTagContainerInvokee implements TagContainerInvokee {

	private List<OptimizedTciInstruction> instructions;

	private record OptimizedTciInstruction(TagFiltering filter, List<TciAction> actions) {

	}

	@Override
	public void applyInstructions(List<TciInstruction> instructions) {
		this.instructions = instructions.stream()
			.map(instruction -> new OptimizedTciInstruction(
				new TciScopeToTagFilterConverter().visit(instruction.scope()),
				instruction.actions()
			))
			.toList();

		// test run to throw upfront
		applyToInvokee(new LinkedHashSet<>());
	}

	public SequencedSet<Tag> applyToInvokee(Set<Tag> tags) {
		Objects.requireNonNull(instructions, "Instructions not set");

		SequencedSet<Tag> copy = new LinkedHashSet<>(tags);

		for (OptimizedTciInstruction instruction : instructions) {
			apply(instruction, copy);
		}

		return copy;
	}

	private void apply(OptimizedTciInstruction instruction, Set<Tag> mutableContainer) {
		switch (instruction.filter) {
			case ContainerFiltering containerFiltering -> {
				apply(containerFiltering, instruction.actions, mutableContainer);
			}
			case TagFilter tagFilter -> {
				apply(tagFilter, instruction.actions, mutableContainer);
			}
		}
	}

	private void apply(ContainerFiltering containerFiltering, List<TciAction> extActions, Set<Tag> mutableContainer) {
		boolean isMatching = containerFiltering.filter(mutableContainer);

		for(TciAction extAction : extActions) {
			if (extAction instanceof TciIsInExpressionWrapper inActions) {
				for (TciAction inAction : inActions.actions()) {
					switch (inAction) {
						case TciAddTag tciAddTag -> addTag(mutableContainer, isMatching, tciAddTag);
						case TciAddUsingFunction tciAddUsingFunction -> throw new NotImplementedException();
						case TciIsInExpressionWrapper tciIsInExpressionWrapper -> throw new UnexpectedExpressionException();
						case TciJustRemoveTag tciJustRemoveTag -> throw new UnexpectedExpressionException();
						case TciModifyTag tciModifyTag -> throw new UnexpectedExpressionException();
						case TciModifyUsingFunction tciModifyUsingFunction -> throw new UnexpectedExpressionException();
						case TciRemoveTag tciRemoveTag -> removeTag(mutableContainer, isMatching, tciRemoveTag);
						case TciRemoveUsingFunction tciRemoveUsingFunction -> throw new NotImplementedException();
					}
				}
			} else {
				throw new UnexpectedExpressionException();
			}
		}
	}

	private void addTag(Set<Tag> mutableContainer, boolean isMatching, TciAddTag tciAddTag) {
		MatchedType mt = MatchedType.resolve(tciAddTag.tag());
		if (mt != MatchedType.NEITHER) {
			throw new UnexpectedExpressionException();
		}
		if (isMatching) {
			mutableContainer.add(new Tag(tciAddTag.tag().category().stringValue(), tciAddTag.tag().tag().stringValue()));
		}
	}

	private void removeTag(Set<Tag> mutableContainer, boolean isMatching, TciRemoveTag tciRemoveTag) {
		MatchedType mt = MatchedType.resolve(tciRemoveTag.tag());
		if (mt != MatchedType.NEITHER) {
			throw new UnexpectedExpressionException();
		}
		if (isMatching) {
			mutableContainer.remove(new Tag(tciRemoveTag.tag().category().stringValue(), tciRemoveTag.tag().tag().stringValue()));
		}
	}

	private void apply(TagFilter tagFilter, List<TciAction> actions, Set<Tag> mutableContainer) {
		Set<Tag> matchingTags = mutableContainer.stream().filter(t -> tagFilter.filter(t)).collect(toSet());
		for (TciAction action : actions) {
			switch (action) {
			case TciAddTag tciAddTag -> addTag(mutableContainer, matchingTags, tciAddTag);
			case TciAddUsingFunction tciAddUsingFunction -> throw new NotImplementedException();
			case TciIsInExpressionWrapper tciIsInExpressionWrapper -> throw new UnexpectedExpressionException();
			case TciJustRemoveTag tciJustRemoveTag -> justRemove(mutableContainer, matchingTags, tciJustRemoveTag);
			case TciModifyTag tciModifyTag -> modifyTag(mutableContainer, matchingTags, tciModifyTag);
			case TciModifyUsingFunction tciModifyUsingFunction -> throw new NotImplementedException();
			case TciRemoveTag tciRemoveTag -> removeTag(mutableContainer, matchingTags, tciRemoveTag);
			case TciRemoveUsingFunction tciRemoveUsingFunction -> throw new NotImplementedException();
			}
		}
	}

	private void addTag(Set<Tag> original, Set<Tag> matching, TciAddTag tciAddTag) {
		MatchedType mt = MatchedType.resolve(tciAddTag.tag());
		switch (mt) {
			case NEITHER -> {
				if (!matching.isEmpty()) {
					original.add(new Tag(tciAddTag.tag().category().stringValue(), tciAddTag.tag().tag().stringValue()));
				}
			}
			case CATEGORY -> {
				matching.forEach(t -> {
					original.add(new Tag(t.category(), tciAddTag.tag().tag().stringValue()));
				});
			}
			case TAG -> {
				matching.forEach(t -> {
					original.add(new Tag(tciAddTag.tag().category().stringValue(), t.tag()));
				});
			}
			case BOTH -> {
				// this is, interestingly, a no-op - adding a tag with the same value that it has
			}
		}
	}

	private void justRemove(Set<Tag> original, Set<Tag> matching, TciJustRemoveTag tciJustRemoveTag) {
		original.removeAll(matching);
	}

	private void modifyTag(Set<Tag> original, Set<Tag> matching, TciModifyTag tciModifyTag) {
		MatchedType mt = MatchedType.resolve(tciModifyTag.tag());
		switch (mt) {
			case NEITHER -> {
				matching.forEach(t -> {
					original.remove(t);
				});
				original.add(new Tag(tciModifyTag.tag().category().stringValue(), tciModifyTag.tag().tag().stringValue()));
			}
			case CATEGORY -> {
				matching.forEach(t -> {
					original.remove(t);
					original.add(new Tag(t.category(), tciModifyTag.tag().tag().stringValue()));
				});
			}
			case TAG -> {
				matching.forEach(t -> {
					original.remove(t);
					original.add(new Tag(tciModifyTag.tag().category().stringValue(), t.tag()));
				});
			}
			case BOTH -> {
				// this is, interestingly, a no-op - modifying a tag to have the same value that it has
			}
		}
	}

	private void removeTag(Set<Tag> original, Set<Tag> matching, TciRemoveTag tciRemoveTag) {
		MatchedType mt = MatchedType.resolve(tciRemoveTag.tag());
		switch (mt) {
			case NEITHER -> {
				original.remove(new Tag(tciRemoveTag.tag().category().stringValue(), tciRemoveTag.tag().tag().stringValue()));
			}
			case CATEGORY -> {
				matching.forEach(t -> {
					original.remove(new Tag(t.category(), tciRemoveTag.tag().tag().stringValue()));
				});
			}
			case TAG -> {
				matching.forEach(t -> {
					original.remove(new Tag(tciRemoveTag.tag().category().stringValue(), t.tag()));
				});
			}
			case BOTH -> {
				original.removeAll(matching);
			}
		}
	}

	private enum MatchedType {
		NEITHER,
		CATEGORY,
		TAG,
		BOTH,
		;

		public static MatchedType resolve(TagStringOrMatched tag) {
			boolean isCategoryActualString = tag.category().isString();
			boolean isTagActualString = tag.tag().isString();

			if (isCategoryActualString && isTagActualString) {
				return NEITHER;
			}
			if (isTagActualString) {
				return CATEGORY;
			}
			if (isCategoryActualString) {
				return TAG;
			}
			return BOTH;
		}
	}
}
