package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.SemanticException;
import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.NotExpression;
import place.sita.tflang.filteringexpression.impl.*;

import java.util.ArrayList;
import java.util.List;

public class FilteringExpressionParser extends TFLangBaseVisitor<FilteringExpression> {

	@Override
	public FilteringExpression visitParseMatchExpression(TFLangParser.ParseMatchExpressionContext ctx) {
		// fallthrough
		return super.visitParseMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitBinaryOpMatchExpression(TFLangParser.BinaryOpMatchExpressionContext ctx) {
		BinaryOpType type = new BinaryOpParser().visit(ctx.binaryOp());
		FilteringExpression left = this.visit(ctx.matchExpression(0));
		FilteringExpression right = this.visit(ctx.matchExpression(1));
		if (type == BinaryOpType.AND) {
			return new AndExpressionImpl(List.of(left, right));
		} else {
			return new OrExpressionImpl(List.of(left, right));
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
		return super.visitParenthesesMatchExpression(ctx);
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
		// fallthrough
		return super.visitSingleMatchExpression(ctx);
	}

	@Override
	public FilteringExpression visitEqComparison(TFLangParser.EqComparisonContext ctx) {
		return new EqualExpressionImpl(
			ctx.NAME().getText(),
			ctx.StringLiteral().getText()
		);
	}

	@Override
	public FilteringExpression visitLikeComparison(TFLangParser.LikeComparisonContext ctx) {
		return new LikeExpressionImpl(
			ctx.NAME().getText(),
			ctx.StringLiteral().getText()
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
		return new InTupleExpressionImpl(keys, values);
	}
}
