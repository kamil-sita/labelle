package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.ModifyExpression;
import place.sita.tflang.modificationexpression.changeexpression.Tuple;

public record ModifyExpressionImpl(Tuple modify) implements ModifyExpression {
}
