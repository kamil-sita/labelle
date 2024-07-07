package place.sita.tflang.modificationexpression.parsing;

import place.sita.tflang.TFLangBaseVisitor;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.TFlangUtil;
import place.sita.tflang.modificationexpression.StringOrMatched;
import place.sita.tflang.modificationexpression.impl.StringOrMatchedImpl;

public class StringOrMatchedParser extends TFLangBaseVisitor<StringOrMatched> {

	@Override
	public StringOrMatched visitCopy(TFLangParser.CopyContext ctx) {
		// todo this have a different impl where accessing stringValue crashes?
		return new StringOrMatchedImpl(false, null);
	}

	@Override
	public StringOrMatched visitString(TFLangParser.StringContext ctx) {
		return new StringOrMatchedImpl(true, TFlangUtil.stripQuotes(ctx.getText()));
	}
}
