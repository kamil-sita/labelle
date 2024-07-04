package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.NotExpression;

public record NotExpressionImpl(FilteringExpression expression) implements NotExpression {
}
