package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.NotExpression;

public record NotExpressionImpl(FilteringExpression expression) implements NotExpression {
}
