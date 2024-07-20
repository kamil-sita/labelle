package place.sita.tflang.filteringexpression;

import place.sita.tflang.filteringexpression.fillteringexpression.*;

public abstract class FilteringExpressionBaseVisitor<T> {

	public T visit(FilteringExpression expression) {
		if (expression == null) {
			throw new IllegalArgumentException("expression cannot be null");
		}
		if (expression == FilteringExpression.MATCH_EVERYTHING) {
			return visitMatchEverything();
		} else if (expression == FilteringExpression.MATCH_NOTHING) {
			return visitMatchNothing();
		}
		return switch (expression) {
			case AndExpression andExpression -> visitAnd(andExpression);
			case EqualExpression equalExpression -> visitEqual(equalExpression);
			// todo redundant?
			case EverythingExpression everythingExpression -> visitEverything(everythingExpression);
			case InExpression inExpression -> visitIn(inExpression);
			case InTupleExpression inTupleExpression -> visitInTuple(inTupleExpression);
			case LikeExpression likeExpression -> visitLike(likeExpression);
			case NotExpression notExpression -> visitNot(notExpression);
			case OrExpression orExpression -> visitOr(orExpression);
			case InSubEntityExpression inSubEntityExpression -> visitInSubEntity(inSubEntityExpression);
		};
	}

	protected abstract T visitInSubEntity(InSubEntityExpression inSubEntityExpression);

	protected abstract T visitMatchEverything();

	protected abstract T visitMatchNothing();

	protected abstract T visitAnd(AndExpression andExpression);

	protected abstract T visitEqual(EqualExpression equalExpression);

	protected abstract T visitEverything(EverythingExpression everythingExpression);

	protected abstract T visitIn(InExpression inExpression);

	protected abstract T visitInTuple(InTupleExpression inTupleExpression);

	protected abstract T visitLike(LikeExpression likeExpression);

	protected abstract T visitNot(NotExpression notExpression);

	protected abstract T visitOr(OrExpression orExpression);

}
