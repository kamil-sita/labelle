package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.AndExpression;
import place.sita.tflang.filteringexpression.FilteringExpression;

import java.util.List;

public record AndExpressionImpl(List<FilteringExpression> expressions) implements AndExpression {
}
