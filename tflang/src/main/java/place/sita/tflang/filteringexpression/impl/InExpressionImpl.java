package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.InExpression;

import java.util.List;

public record InExpressionImpl(String key, List<String> values) implements InExpression {
}
