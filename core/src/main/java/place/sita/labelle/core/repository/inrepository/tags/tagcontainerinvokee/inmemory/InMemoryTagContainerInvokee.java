package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import org.apache.commons.lang3.NotImplementedException;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.TagContainerInvokee;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.TagStringOrMatched;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.TciInstruction;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.action.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class InMemoryTagContainerInvokee implements TagContainerInvokee {

	private List<OptimizedTciInstruction> instructions;

	private record OptimizedTciInstruction(TagFilter filter, List<TciAction> actions) {

	}

	@Override
	public void applyInstructions(List<TciInstruction> instructions) {
		this.instructions = instructions.stream()
			.map(instruction -> new OptimizedTciInstruction(
				new TciScopeToTagFilterConverter().visit(instruction.scope()),
				instruction.actions()
			))
			.toList();
	}

	public Set<Tag> applyToInvokee(Set<Tag> tags) {
		if (instructions == null) {
			throw new IllegalStateException("Instructions not set");
		}

		Set<Tag> copy = new LinkedHashSet<>(tags);

		for (OptimizedTciInstruction instruction : instructions) {
			apply(instruction, copy);
		}

		return copy;
	}

	private void apply(OptimizedTciInstruction instruction, Set<Tag> copy) {
		Set<Tag> matchingTags = copy.stream().filter(t -> instruction.filter.filter(t)).collect(toSet());

		for (TciAction action : instruction.actions) {
			switch (action) {
				case TciAddTag tciAddTag -> addTag(copy, matchingTags, tciAddTag);
				case TciAddUsingFunction tciAddUsingFunction -> throw new NotImplementedException();
				case TciIsInExpressionWrapper tciIsInExpressionWrapper -> throw new NotImplementedException();
				case TciJustRemoveTag tciJustRemoveTag -> justRemove(copy, matchingTags, tciJustRemoveTag);
				case TciModifyTag tciModifyTag -> modifyTag(copy, matchingTags, tciModifyTag);
				case TciModifyUsingFunction tciModifyUsingFunction -> throw new NotImplementedException();
				case TciRemoveTag tciRemoveTag -> removeTag(copy, matchingTags, tciRemoveTag);
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
