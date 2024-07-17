package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee;

import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.action.*;
import place.sita.tflang.modificationexpression.ChangeExpressionBaseVisitor;
import place.sita.tflang.modificationexpression.changeexpression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TFlangChangeToTciAction extends ChangeExpressionBaseVisitor<List<TciAction>> {
	@Override
	protected List<TciAction> visitAddExpression(AddExpression addExpression) {
		TagStringOrMatched parse = parseTuple(addExpression.add());
		return List.of(new TciAddTag(parse));
	}

	@Override
	protected List<TciAction> visitAddUsingFunctionExpression(AddUsingFunctionExpression addUsingFunctionExpression) {
		return List.of(new TciAddUsingFunction(addUsingFunctionExpression.functionName()));
	}

	@Override
	protected List<TciAction> visitChangeInEntityExpression(ChangeInEntityExpression changeInEntityExpression) {
		if (!Objects.equals(changeInEntityExpression.entityName(), "tags")) {
			throw new ConversionException("Only tags entity is supported");
		}

		List<TciAction> actions = visit(changeInEntityExpression.change());

		return List.of(
			new TciIsInExpressionWrapper(actions)
		);
	}

	@Override
	protected List<TciAction> visitJustRemoveExpression(JustRemoveExpression justRemoveExpression) {
		return List.of(new TciJustRemoveTag());
	}

	@Override
	protected List<TciAction> visitModifyExpression(ModifyExpression modifyExpression) {
		TagStringOrMatched parse = parseTuple(modifyExpression.modify());
		return List.of(new TciModifyTag(parse));
	}

	@Override
	protected List<TciAction> visitModifyUsingFunctionExpression(ModifyUsingFunctionExpression modifyUsingFunctionExpression) {
		return List.of(new TciModifyUsingFunction(modifyUsingFunctionExpression.functionName()));
	}

	@Override
	protected List<TciAction> visitMultiChangeExpression(MultiChangeExpression multiChangeExpression) {
		List<TciAction> actions = new ArrayList<>();

		for (ChangeExpression change : multiChangeExpression.changes()) {
			actions.addAll(visit(change));
		}

		return actions;
	}

	@Override
	protected List<TciAction> visitRemoveExpression(RemoveExpression removeExpression) {
		TagStringOrMatched parse = parseTuple(removeExpression.remove());
		return List.of(new TciRemoveTag(parse));
	}

	@Override
	protected List<TciAction> visitRemoveUsingFunctionExpression(RemoveUsingFunctionExpression removeUsingFunctionExpression) {
		return List.of(new TciRemoveUsingFunction(removeUsingFunctionExpression.functionName()));
	}

	private TagStringOrMatched parseTuple(Tuple tuple) {
		if (tuple.dimensionality() != 2) {
			throw new ConversionException("Tuple must have dimensionality of 2");
		}

		StringOrMatched category = tuple.valueAt(0);
		StringOrMatched tag = tuple.valueAt(1);

		return new TagStringOrMatched(category, tag);
	}
}
