package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.OrExpression;

import java.util.List;

public record OrExpressionImpl(List<FilteringExpression> expressions) implements OrExpression {
}
