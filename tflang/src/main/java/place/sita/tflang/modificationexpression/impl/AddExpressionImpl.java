package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.AddExpression;
import place.sita.tflang.modificationexpression.changeexpression.Tuple;

public record AddExpressionImpl(Tuple add) implements AddExpression {
}
