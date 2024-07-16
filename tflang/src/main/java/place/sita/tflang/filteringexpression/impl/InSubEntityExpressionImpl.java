package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.InSubEntityExpression;

public record InSubEntityExpressionImpl(String subEntity, FilteringExpression expression) implements
		InSubEntityExpression {

}
