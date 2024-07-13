package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.changeexpression.MultiChangeExpression;

import java.util.List;

public record MultiChangeExpressionImpl(List<ChangeExpression> changes) implements MultiChangeExpression {

}
