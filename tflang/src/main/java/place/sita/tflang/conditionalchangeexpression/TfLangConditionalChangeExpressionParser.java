package place.sita.tflang.conditionalchangeexpression;

import place.sita.tflang.ConditionalChangeExpression;
import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.parsing.TFlangFilteringExpressionParser;
import place.sita.tflang.modificationexpression.changeexpression.ChangeExpression;
import place.sita.tflang.modificationexpression.parsing.TFLangModificationExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TfLangConditionalChangeExpressionParser extends TFLangBaseVisitor<List<ConditionalChangeExpression>> {

	@Override
	public List<ConditionalChangeExpression> visitParseConditionalChangeExpressionMany(TFLangParser.ParseConditionalChangeExpressionManyContext ctx) {
		return visitConditionalChangeExpressionMany(ctx.conditionalChangeExpressionMany());
	}

	@Override
	public List<ConditionalChangeExpression> visitConditionalChangeExpressionMany(TFLangParser.ConditionalChangeExpressionManyContext ctx) {
		List<ConditionalChangeExpression> expressions = new ArrayList<>();

		for (var expression : ctx.conditionalChangeExpression()) {
			expressions.addAll(visit(expression));
		}

		return expressions;
	}

	@Override
	public List<ConditionalChangeExpression> visitConditionalChangeExpression(TFLangParser.ConditionalChangeExpressionContext ctx) {

		FilteringExpression expression;

		if (ctx.matchExpression() != null) {
			expression = new TFlangFilteringExpressionParser().visit(ctx.matchExpression());
		} else {
			expression = FilteringExpression.MATCH_EVERYTHING;
		}
		Objects.requireNonNull(expression);

		ChangeExpression changeExpression = new TFLangModificationExpressionParser().visit(ctx.changeManyExpression());

		return List.of(new ConditionalChangeExpression(expression, changeExpression));
	}
}
