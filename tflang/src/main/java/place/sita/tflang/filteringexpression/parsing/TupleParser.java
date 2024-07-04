package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.NotExpression;
import place.sita.tflang.filteringexpression.impl.*;

import java.util.ArrayList;
import java.util.List;

import static place.sita.tflang.TFlangUtil.stripQuotes;

public class TupleParser extends TFLangBaseVisitor<List<String>> {

	@Override
	public List<String> visitNameTuple(TFLangParser.NameTupleContext ctx) {
		return ctx.NAME()
			.stream()
			.map(v -> v.getText())
			.toList();
	}

	@Override
	public List<String> visitValueTuple(TFLangParser.ValueTupleContext ctx) {
		// fallthrough
		return super.visitValueTuple(ctx);
	}

	@Override
	public List<String> visitStringList(TFLangParser.StringListContext ctx) {
		return ctx.StringLiteral()
			.stream()
			.map(v -> stripQuotes(v.getText()))
			.toList();
	}
}
