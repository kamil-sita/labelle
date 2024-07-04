package place.sita.tflang.filteringexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;

public class BinaryOpParser extends TFLangBaseVisitor<BinaryOpType> {

	@Override
	public BinaryOpType visitAnd(TFLangParser.AndContext ctx) {
		return BinaryOpType.AND;
	}

	@Override
	public BinaryOpType visitOr(TFLangParser.OrContext ctx) {
		return BinaryOpType.OR;
	}
}
