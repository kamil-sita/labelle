package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.*;
import place.sita.tflang.filteringexpression.FilteringExpressionBaseVisitor;
import place.sita.tflang.filteringexpression.fillteringexpression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TFlangFilteringToTciScope extends FilteringExpressionBaseVisitor<TciScope>  {

	protected static final String CATEGORY = "category";
	protected static final String TAG = "tag";

	@Override
	protected TciScope visitInSubEntity(InSubEntityExpression inSubEntityExpression) {
		if (Objects.equals("tags", inSubEntityExpression.subEntity())) {
			return new TciExists(visit(inSubEntityExpression.expression()));
		} else {
			throw new ConversionException("Unknown subEntity: " + inSubEntityExpression.subEntity() + ". Expected: tags");
		}
	}

	@Override
	protected TciScope visitMatchEverything() {
		return new TciScopeAll();
	}

	@Override
	protected TciScope visitMatchNothing() {
		return new TciScopeNot(new TciScopeAll());
	}

	@Override
	protected TciScope visitAnd(AndExpression andExpression) {
		List<TciScope> all = new ArrayList<>();

		for (FilteringExpression expression : andExpression.expressions()) {
			TciScope scope = visit(expression);
			all.add(scope);
		}

		return new TciScopeAnd(all);
	}

	@Override
	protected TciScope visitEqual(EqualExpression equalExpression) {
		return switch (equalExpression.key()) {
			case CATEGORY -> new TciScopeCategoryEqual(equalExpression.value());
			case TAG -> new TciScopeTagEqual(equalExpression.value());
			default ->
				throw new ConversionException("Unknown key: " + equalExpression.key() + ". Expected one of: " + CATEGORY + ", " + TAG);
		};
	}

	@Override
	protected TciScope visitEverything(EverythingExpression everythingExpression) {
		return new TciScopeAll();
	}

	@Override
	protected TciScope visitIn(InExpression inExpression) {
		return switch (inExpression.key()) {
			case CATEGORY -> new TciScopeCategoryIn(inExpression.values());
			case TAG -> new TciScopeTagIn(inExpression.values());
			default ->
				throw new ConversionException("Unknown key: " + inExpression.key() + ". Expected one of: " + CATEGORY + ", " + TAG);
		};
	}

	@Override
	protected TciScope visitInTuple(InTupleExpression inTupleExpression) {
		int categoryIndex = inTupleExpression.keys().indexOf(CATEGORY);
		int tagIndex = inTupleExpression.keys().indexOf(TAG);
		if (categoryIndex == -1 && tagIndex == -1) {
			throw new ConversionException("Expected keys: " + CATEGORY + ", " + TAG);
		}
		int validKeysCount = (categoryIndex != -1 ? 1 : 0) + (tagIndex != -1 ? 1 : 0);
		if (validKeysCount != inTupleExpression.keys().size()) {
			throw new ConversionException("Expected keys: " + CATEGORY + ", " + TAG);
		}
		if (validKeysCount == 1) {
			List<String> tupleValues = flatten(inTupleExpression.values());
			if (categoryIndex != -1) {
				return new TciScopeCategoryIn(tupleValues);
			} else {
				return new TciScopeTagIn(tupleValues);
			}
		} else {
			List<Tag> values = new ArrayList<>();
			for (int i = 0; i < inTupleExpression.values().size(); i++) {
				Tag tag = new Tag(
					inTupleExpression.values().get(i).get(categoryIndex),
					inTupleExpression.values().get(i).get(tagIndex)
				);
				values.add(tag);
			}
			return new TciScopeCategoryTagIn(values);
		}
	}

	private List<String> flatten(List<List<String>> values) {
		List<String> result = new ArrayList<>();
		for (List<String> value : values) {
			result.addAll(value);
		}
		return result;
	}

	@Override
	protected TciScope visitLike(LikeExpression likeExpression) {
		return switch (likeExpression.key()) {
			case CATEGORY -> new TciScopeCategoryLike(likeExpression.value());
			case TAG -> new TciScopeTagLike(likeExpression.value());
			default ->
				throw new ConversionException("Unknown key: " + likeExpression.key() + ". Expected one of: " + CATEGORY + ", " + TAG);
		};
	}

	@Override
	protected TciScope visitNot(NotExpression notExpression) {
		TciScope scope = visit(notExpression.expression());
		return new TciScopeNot(scope);
	}

	@Override
	protected TciScope visitOr(OrExpression orExpression) {
		List<TciScope> all = new ArrayList<>();

		for (FilteringExpression expression : orExpression.expressions()) {
			TciScope scope = visit(expression);
			all.add(scope);
		}

		return new TciScopeOr(all);
	}
}
