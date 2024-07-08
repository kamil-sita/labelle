package place.sita.tflang.filteringexpression.impl;

import place.sita.tflang.filteringexpression.fillteringexpression.InTupleExpression;

import java.util.List;

public record InTupleExpressionImpl(List<String> keys, List<List<String>> values) implements InTupleExpression {
}
