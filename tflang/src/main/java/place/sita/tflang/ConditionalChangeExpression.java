package place.sita.tflang;

import place.sita.tflang.filteringexpression.FilteringExpression;

public interface ConditionalChangeExpression {

	FilteringExpression filter();

	ChangeExpression change();

}
