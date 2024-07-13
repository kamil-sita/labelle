package place.sita.tflang.modificationexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.modificationexpression.changeexpression.StringOrMatched;
import place.sita.tflang.modificationexpression.changeexpression.Tuple;
import place.sita.tflang.modificationexpression.impl.TupleImpl;

import java.util.ArrayList;
import java.util.List;

public class MatchedTupleParser extends TFLangBaseVisitor<Tuple> {

	@Override
	public Tuple visitMatchedOrStringTupleExpression(TFLangParser.MatchedOrStringTupleExpressionContext ctx) {
		// fallthrough
		return super.visitMatchedOrStringTupleExpression(ctx);
	}

	@Override
	public Tuple visitMatchedOrStringTuple(TFLangParser.MatchedOrStringTupleContext ctx) {
		List<StringOrMatched> values = new ArrayList<>();

		for (TFLangParser.MatchedOrStringContext value : ctx.matchedOrString()) {
			values.add(new StringOrMatchedParser().visit(value));
		}

		return new TupleImpl(values);
	}
}
