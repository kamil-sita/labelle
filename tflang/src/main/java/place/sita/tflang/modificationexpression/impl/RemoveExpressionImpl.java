package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.RemoveExpression;
import place.sita.tflang.modificationexpression.Tuple;

public record RemoveExpressionImpl(Tuple remove) implements RemoveExpression {
}
