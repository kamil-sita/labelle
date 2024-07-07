package place.sita.tflang.modificationexpression;

public sealed interface ChangeExpression permits AddExpression, AddUsingFunctionExpression, ChangeInEntityExpression, JustRemoveExpression, ModifyExpression, ModifyUsingFunctionExpression, MultiChangeExpression, RemoveExpression, RemoveUsingFunctionExpression {
}
