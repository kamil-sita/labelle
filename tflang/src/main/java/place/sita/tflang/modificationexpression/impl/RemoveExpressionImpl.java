package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.RemoveExpression;
import place.sita.tflang.modificationexpression.changeexpression.Tuple;

public record RemoveExpressionImpl(Tuple remove) implements RemoveExpression {
}
