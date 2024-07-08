package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.AndExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;

import java.util.List;

public record AndExpressionImpl(List<FilteringExpression> expressions) implements AndExpression {
}
