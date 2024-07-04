package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.impl.OrExpressionImpl;

import java.util.ArrayList;
import java.util.List;

public class FilteringExpressionParser extends TFLangBaseVisitor<FilteringExpression> {

	@Override
	public FilteringExpression visitParseMatchExpression(TFLangParser.ParseMatchExpressionContext ctx) {
		return super.visitParseMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitMatchExpression(TFLangParser.MatchExpressionContext ctx) {
		return super.visitMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitAny(TFLangParser.AnyContext ctx) {
		return FilteringExpression.MATCH_EVERYTHING;
	}

	@Override
	public FilteringExpression visitAnd(TFLangParser.AndContext ctx) {
		List<FilteringExpression> expressions = new ArrayList<>();
		for (var child : ctx.children) {
			FilteringExpression filteringExpression = this.visit(child);
			if (filteringExpression.equals(FilteringExpression.MATCH_NOTHING)) {
				return FilteringExpression.MATCH_NOTHING;
			}
			if (filteringExpression.equals(FilteringExpression.MATCH_EVERYTHING)) {
				continue;
			}
			expressions.add(filteringExpression);
		}
		if (expressions.isEmpty()) {
			return FilteringExpression.MATCH_EVERYTHING;
		}
		if (expressions.size() == 1) {
			return expressions.get(0);
		}
		return new OrExpressionImpl(expressions);
	}

	@Override
	public FilteringExpression visitOr(TFLangParser.OrContext ctx) {
		List<FilteringExpression> expressions = new ArrayList<>();
		for (var child : ctx.children) {
			FilteringExpression filteringExpression = this.visit(child);
			if (filteringExpression.equals(FilteringExpression.MATCH_EVERYTHING)) {
				return FilteringExpression.MATCH_EVERYTHING;
			}
			if (filteringExpression.equals(FilteringExpression.MATCH_NOTHING)) {
				continue;
			}
			expressions.add(filteringExpression);
		}
		if (expressions.isEmpty()) {
			return FilteringExpression.MATCH_EVERYTHING;
		}
		if (expressions.size() == 1) {
			return expressions.get(0);
		}
		return new OrExpressionImpl(expressions);
	}

	@Override
	public FilteringExpression visitSingleMatchExpression(TFLangParser.SingleMatchExpressionContext ctx) {
		return super.visitSingleMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitEqComparison(TFLangParser.EqComparisonContext ctx) {
		return super.visitEqComparison(ctx);
	}

	@Override
	public FilteringExpression visitLikeComparison(TFLangParser.LikeComparisonContext ctx) {
		return super.visitLikeComparison(ctx);
	}

	@Override
	public FilteringExpression visitInComparison(TFLangParser.InComparisonContext ctx) {
		return super.visitInComparison(ctx);
	}

	@Override
	public FilteringExpression visitTupleEqComparison(TFLangParser.TupleEqComparisonContext ctx) {
		return super.visitTupleEqComparison(ctx);
	}

	@Override
	public FilteringExpression visitInEqComparison(TFLangParser.InEqComparisonContext ctx) {
		return super.visitInEqComparison(ctx);
	}


}
