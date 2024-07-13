package place.sita.tflang.modificationexpression.changeexpression;

public sealed interface ChangeExpression permits AddExpression, AddUsingFunctionExpression, ChangeInEntityExpression, JustRemoveExpression, ModifyExpression, ModifyUsingFunctionExpression, MultiChangeExpression, RemoveExpression, RemoveUsingFunctionExpression {
}
