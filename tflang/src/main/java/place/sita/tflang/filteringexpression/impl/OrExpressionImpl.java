package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.OrExpression;

import java.util.List;

public record OrExpressionImpl(List<FilteringExpression> expressions) implements OrExpression {
}
