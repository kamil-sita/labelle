package place.sita.tflang.modificationexpression;

import place.sita.tflang.modificationexpression.changeexpression.*;

public abstract class ChangeExpressionBaseVisitor<T> {


	public T visit(ChangeExpression changeExpression) {
		return switch (changeExpression) {
			case AddExpression addExpression -> visitAddExpression(addExpression);
			case AddUsingFunctionExpression addUsingFunctionExpression -> visitAddUsingFunctionExpression(addUsingFunctionExpression);
			case ChangeInEntityExpression changeInEntityExpression -> visitChangeInEntityExpression(changeInEntityExpression);
			case JustRemoveExpression justRemoveExpression -> visitJustRemoveExpression(justRemoveExpression);
			case ModifyExpression modifyExpression -> visitModifyExpression(modifyExpression);
			case ModifyUsingFunctionExpression modifyUsingFunctionExpression -> visitModifyUsingFunctionExpression(modifyUsingFunctionExpression);
			case MultiChangeExpression multiChangeExpression -> visitMultiChangeExpression(multiChangeExpression);
			case RemoveExpression removeExpression -> visitRemoveExpression(removeExpression);
			case RemoveUsingFunctionExpression removeUsingFunctionExpression -> visitRemoveUsingFunctionExpression(removeUsingFunctionExpression);
		};
	}

	protected abstract T visitAddExpression(AddExpression addExpression);

	protected abstract T visitAddUsingFunctionExpression(AddUsingFunctionExpression addUsingFunctionExpression);

	protected abstract T visitChangeInEntityExpression(ChangeInEntityExpression changeInEntityExpression);

	protected abstract T visitJustRemoveExpression(JustRemoveExpression justRemoveExpression);

	protected abstract T visitModifyExpression(ModifyExpression modifyExpression);

	protected abstract T visitModifyUsingFunctionExpression(ModifyUsingFunctionExpression modifyUsingFunctionExpression);

	protected abstract T visitMultiChangeExpression(MultiChangeExpression multiChangeExpression);

	protected abstract T visitRemoveExpression(RemoveExpression removeExpression);

	protected abstract T visitRemoveUsingFunctionExpression(RemoveUsingFunctionExpression removeUsingFunctionExpression);


}
