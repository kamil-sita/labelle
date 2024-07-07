package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.MultiChangeExpression;

import java.util.List;

public record MultiChangeExpressionImpl(List<ChangeExpression> changes) implements MultiChangeExpression {

}
