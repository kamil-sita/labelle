package place.sita.tflang.modificationexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.modificationexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.impl.*;

import java.util.ArrayList;
import java.util.List;

public class TFLangModificationExpressionParser extends TFLangBaseVisitor<ChangeExpression> {

	@Override
	public ChangeExpression visitChangeManyExpressionParse(TFLangParser.ChangeManyExpressionParseContext ctx) {
		return visit(ctx.changeManyExpression());
	}

	@Override
	public ChangeExpression visitChangeManyExpression(TFLangParser.ChangeManyExpressionContext ctx) {

		List<ChangeExpression> expressions = new ArrayList<>();

		for (TFLangParser.ChangeExpressionContext expression : ctx.changeExpression()) {
			expressions.add(new TFLangModificationExpressionParser().visit(expression));
		}

		if (expressions.size() == 1) {
			return expressions.get(0);
		}

		return new MultiChangeExpressionImpl(expressions);
	}

	@Override
	public ChangeExpression visitChangeInEntityExpression(TFLangParser.ChangeInEntityExpressionContext ctx) {
		ChangeExpression expression = visit(ctx.changeManyExpression());

		return new ChangeInEntityExpressionImpl(
			ctx.NAME().getText(),
			expression
		);
	}

	@Override
	public ChangeExpression visitAddManyExpressionCalculated(TFLangParser.AddManyExpressionCalculatedContext ctx) {
		List<ChangeExpression> changeExpressions = new ArrayList<>();

		for (TFLangParser.MatchedOrStringTupleExpressionContext tupleExpr : ctx.matchedOrStringTupleExpression()) {
			changeExpressions.add(
				new AddExpressionImpl(
					new MatchedTupleParser().visit(tupleExpr)
				)
			);
		}

		if (changeExpressions.size() == 1) {
			return changeExpressions.get(0);
		}

		return new MultiChangeExpressionImpl(changeExpressions);
	}

	@Override
	public ChangeExpression visitAddManyExpressionSpecial(TFLangParser.AddManyExpressionSpecialContext ctx) {
		return new AddUsingFunctionExpressionImpl(ctx.NAME().getText());
	}

	@Override
	public ChangeExpression visitJustRemoveExpression(TFLangParser.JustRemoveExpressionContext ctx) {
		return new JustRemoveExpressionImpl();
	}

	@Override
	public ChangeExpression visitRemoveManyExpressionCalculated(TFLangParser.RemoveManyExpressionCalculatedContext ctx) {
		List<ChangeExpression> changeExpressions = new ArrayList<>();

		for (TFLangParser.MatchedOrStringTupleExpressionContext tupleExpr : ctx.matchedOrStringTupleExpression()) {
			changeExpressions.add(
				new RemoveExpressionImpl(
					new MatchedTupleParser().visit(tupleExpr)
				)
			);
		}

		if (changeExpressions.size() == 1) {
			return changeExpressions.get(0);
		}

		return new MultiChangeExpressionImpl(changeExpressions);
	}

	@Override
	public ChangeExpression visitRemoveManyExpressionSpecial(TFLangParser.RemoveManyExpressionSpecialContext ctx) {
		return new RemoveUsingFunctionExpressionImpl(ctx.NAME().getText());
	}

	@Override
	public ChangeExpression visitTransformExpressionCalculated(TFLangParser.TransformExpressionCalculatedContext ctx) {
		return new ModifyExpressionImpl(
			new MatchedTupleParser().visit(ctx.matchedOrStringTupleExpression())
		);

	}

	@Override
	public ChangeExpression visitTransformExpressionSpecial(TFLangParser.TransformExpressionSpecialContext ctx) {
		return new ModifyUsingFunctionExpressionImpl(ctx.NAME().getText());
	}
}
