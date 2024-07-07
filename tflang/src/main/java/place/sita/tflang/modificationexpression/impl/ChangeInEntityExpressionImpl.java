package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.ChangeInEntityExpression;

public record ChangeInEntityExpressionImpl(String entityName, ChangeExpression change) implements ChangeInEntityExpression {
}
