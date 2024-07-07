package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.*;
import place.sita.tflang.filteringexpression.AndExpression;
import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.NotExpression;
import place.sita.tflang.filteringexpression.OrExpression;
import place.sita.tflang.filteringexpression.impl.*;

import java.util.ArrayList;
import java.util.List;

import static place.sita.tflang.TFlangUtil.stripQuotes;

public class TFlangFilteringExpressionParser extends TFLang_searchingBaseVisitor<FilteringExpression> {

	@Override
	public FilteringExpression visitParseMatchExpression(TFLang_searchingParser.ParseMatchExpressionContext ctx) {
		return visit(ctx.matchExpression());
	}

	@Override
	public FilteringExpression visitBinaryOpMatchExpression(TFLang_searchingParser.BinaryOpMatchExpressionContext ctx) {
		BinaryOpType type = new BinaryOpParser().visit(ctx.binaryOp());
		FilteringExpression left = this.visit(ctx.matchExpression(0));
		FilteringExpression right = this.visit(ctx.matchExpression(1));
		if (type == BinaryOpType.AND) {
			if (left instanceof AndExpression andExpression) {
				List<FilteringExpression> expressions = new ArrayList<>(andExpression.expressions());
				expressions.add(right);
				return visitAnd(expressions);
			}
			return visitAnd(List.of(left, right));
		} else {
			if (left instanceof OrExpression orExpression) {
				List<FilteringExpression> expressions = new ArrayList<>(orExpression.expressions());
				expressions.add(right);
				return visitOr(expressions);
			}
			return visitOr(List.of(left, right));
		}
	}

	@Override
	public FilteringExpression visitUnaryOpMatchExpression(TFLang_searchingParser.UnaryOpMatchExpressionContext ctx) {
		FilteringExpression underlying = this.visit(ctx.matchExpression());
		if (underlying instanceof NotExpression notExpression) {
			return notExpression.expression();
		}
		return new NotExpressionImpl(underlying);
	}

	@Override
	public FilteringExpression visitParenthesesMatchExpression(TFLang_searchingParser.ParenthesesMatchExpressionContext ctx) {
		// fallthrough
		return super.visitParenthesesMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitSingleMatchMatchExpression(TFLang_searchingParser.SingleMatchMatchExpressionContext ctx) {
		// fallthrough
		return super.visitSingleMatchMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitAnyMatchExpression(TFLang_searchingParser.AnyMatchExpressionContext ctx) {
		// fallthrough
		return super.visitAnyMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitAny(TFLang_searchingParser.AnyContext ctx) {
		return FilteringExpression.MATCH_EVERYTHING;
	}

	private FilteringExpression visitAnd(List<FilteringExpression> filteringExpressions) {
		List<FilteringExpression> expressions = new ArrayList<>();
		for (var child : filteringExpressions) {
			if (child.equals(FilteringExpression.MATCH_NOTHING)) {
				return FilteringExpression.MATCH_NOTHING;
			}
			if (child.equals(FilteringExpression.MATCH_EVERYTHING)) {
				continue;
			}
			expressions.add(child);
		}
		if (expressions.isEmpty()) {
			return FilteringExpression.MATCH_EVERYTHING;
		}
		if (expressions.size() == 1) {
			return expressions.get(0);
		}
		return new AndExpressionImpl(expressions);
	}

	private FilteringExpression visitOr(List<FilteringExpression> filteringExpressions) {
		List<FilteringExpression> expressions = new ArrayList<>();
		for (var child : filteringExpressions) {
			if (child.equals(FilteringExpression.MATCH_EVERYTHING)) {
				return FilteringExpression.MATCH_EVERYTHING;
			}
			if (child.equals(FilteringExpression.MATCH_NOTHING)) {
				continue;
			}
			expressions.add(child);
		}
		if (expressions.isEmpty()) {
			return FilteringExpression.MATCH_NOTHING;
		}
		if (expressions.size() == 1) {
			return expressions.get(0);
		}
		return new OrExpressionImpl(expressions);
	}

	@Override
	public FilteringExpression visitSingleMatchExpression(TFLang_searchingParser.SingleMatchExpressionContext ctx) {
		// fallthrough
		return super.visitSingleMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitEqComparison(TFLang_searchingParser.EqComparisonContext ctx) {
		return new EqualExpressionImpl(
			ctx.NAME().getText(),
			stripQuotes(ctx.StringLiteral().getText())
		);
	}

	@Override
	public FilteringExpression visitLikeComparison(TFLang_searchingParser.LikeComparisonContext ctx) {
		return new LikeExpressionImpl(
			ctx.NAME().getText(),
			stripQuotes(ctx.StringLiteral().getText())
		);
	}

	@Override
	public FilteringExpression visitInComparison(TFLang_searchingParser.InComparisonContext ctx) {
		String key = ctx.NAME().getText();

		List<String> in = new TupleParser().visit(ctx.stringList());

		if (in.size() == 1) {
			return new EqualExpressionImpl(key, in.get(0));
		} else {
			return new InExpressionImpl(key, in);
		}
	}

	@Override
	public FilteringExpression visitTupleEqComparison(TFLang_searchingParser.TupleEqComparisonContext ctx) {
		List<String> keys = new TupleParser().visit(ctx.nameTuple());
		List<String> values = new TupleParser().visit(ctx.valueTuple());

		if (keys.size() != values.size()) {
			throw new SemanticException("Tuple keys and values must be the same length");
		}

		if (keys.size() == 1) {
			return new EqualExpressionImpl(keys.get(0), values.get(0));
		} else {
			List<FilteringExpression> expressions = new ArrayList<>();
			for (int i = 0; i < keys.size(); i++) {
				expressions.add(new EqualExpressionImpl(keys.get(i), values.get(i)));
			}
			return new AndExpressionImpl(expressions);
		}
	}

	@Override
	public FilteringExpression visitInEqComparison(TFLang_searchingParser.InEqComparisonContext ctx) {
		List<String> keys = new TupleParser().visit(ctx.nameTuple());
		List<List<String>> values = new ArrayList<>();
		for (var valueTuple : ctx.valueTuple()) {
			List<String> lValues = new TupleParser().visit(valueTuple);
			if (lValues.size() != keys.size()) {
				throw new SemanticException("Tuple keys and values must be the same length");
			}
			values.add(lValues);
		}
		if (keys.size() == 1) {
			List<FilteringExpression> expressions = new ArrayList<>();
			for (var value : values) {
				expressions.add(new EqualExpressionImpl(keys.get(0), value.get(0)));
			}
			return visitOr(expressions);
		} else {
			return new InTupleExpressionImpl(keys, values);
		}
	}
}
