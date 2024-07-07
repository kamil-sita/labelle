package place.sita.tflang;

import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.modificationexpression.ChangeExpression;

public interface ConditionalChangeExpression {

	FilteringExpression filter();

	ChangeExpression change();

}
