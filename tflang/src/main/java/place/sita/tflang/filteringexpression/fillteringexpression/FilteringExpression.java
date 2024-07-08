package place.sita.tflang.filteringexpression.fillteringexpression;

import place.sita.tflang.filteringexpression.impl.EverythingExpressionImpl;
import place.sita.tflang.filteringexpression.impl.NotExpressionImpl;

public sealed interface FilteringExpression permits
	AndExpression,
	EqualExpression,
	EverythingExpression,
	InExpression,
	InTupleExpression,
	LikeExpression,
	NotExpression,
	OrExpression {

	FilteringExpression MATCH_EVERYTHING = EverythingExpressionImpl.INSTANCE;

	FilteringExpression MATCH_NOTHING = new NotExpressionImpl(MATCH_EVERYTHING);


}
