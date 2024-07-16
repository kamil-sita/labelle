package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.SemanticException;
import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.fillteringexpression.AndExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.NotExpression;
import place.sita.tflang.filteringexpression.fillteringexpression.OrExpression;
import place.sita.tflang.filteringexpression.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static place.sita.tflang.TFlangUtil.stripQuotes;

public class TFlangFilteringExpressionParser extends TFLangBaseVisitor<FilteringExpression> {

	@Override
	public FilteringExpression visitMatchInnerExpression(TFLangParser.MatchInnerExpressionContext ctx) {
		FilteringExpression expression = visit(ctx.matchExpression());
		Objects.requireNonNull(expression);
		return new InSubEntityExpressionImpl(ctx.NAME().getText(), expression);
	}

	@Override
	public FilteringExpression visitParseMatchExpression(TFLangParser.ParseMatchExpressionContext ctx) {
		return visit(ctx.matchExpression());
	}

	@Override
	public FilteringExpression visitBinaryOpMatchExpression(TFLangParser.BinaryOpMatchExpressionContext ctx) {
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
	public FilteringExpression visitUnaryOpMatchExpression(TFLangParser.UnaryOpMatchExpressionContext ctx) {
		FilteringExpression underlying = this.visit(ctx.matchExpression());
		if (underlying instanceof NotExpression notExpression) {
			return notExpression.expression();
		}
		return new NotExpressionImpl(underlying);
	}

	@Override
	public FilteringExpression visitParenthesesMatchExpression(TFLangParser.ParenthesesMatchExpressionContext ctx) {
		// fallthrough
		return visit(ctx.matchExpression());
	}

	@Override
	public FilteringExpression visitSingleMatchMatchExpression(TFLangParser.SingleMatchMatchExpressionContext ctx) {
		// fallthrough
		return super.visitSingleMatchMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitAnyMatchExpression(TFLangParser.AnyMatchExpressionContext ctx) {
		// fallthrough
		return super.visitAnyMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitAny(TFLangParser.AnyContext ctx) {
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
	public FilteringExpression visitSingleMatchExpression(TFLangParser.SingleMatchExpressionContext ctx) {
		// fallthrough
		return super.visitSingleMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitEqComparison(TFLangParser.EqComparisonContext ctx) {
		return new EqualExpressionImpl(
			ctx.NAME().getText(),
			stripQuotes(ctx.StringLiteral().getText())
		);
	}

	@Override
	public FilteringExpression visitLikeComparison(TFLangParser.LikeComparisonContext ctx) {
		return new LikeExpressionImpl(
			ctx.NAME().getText(),
			stripQuotes(ctx.StringLiteral().getText())
		);
	}

	@Override
	public FilteringExpression visitInComparison(TFLangParser.InComparisonContext ctx) {
		String key = ctx.NAME().getText();

		List<String> in = new TupleParser().visit(ctx.stringList());

		if (in.size() == 1) {
			return new EqualExpressionImpl(key, in.get(0));
		} else {
			return new InExpressionImpl(key, in);
		}
	}

	@Override
	public FilteringExpression visitTupleEqComparison(TFLangParser.TupleEqComparisonContext ctx) {
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
	public FilteringExpression visitInEqComparison(TFLangParser.InEqComparisonContext ctx) {
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
