package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.changeexpression.ChangeInEntityExpression;

public record ChangeInEntityExpressionImpl(String entityName, ChangeExpression change) implements ChangeInEntityExpression {
}
