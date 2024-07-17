package place.sita.tflang.parsers;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import place.sita.tflang.ConditionalChangeExpression;
import place.sita.tflang.TFLangLexer;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.conditionalchangeexpression.TfLangConditionalChangeExpressionParser;

import java.util.List;

public class StringToMultipleConditionalChangeExpressionParser {

	public static List<ConditionalChangeExpression> parse(String query) {
		return parse(query, null);
	}

	public static List<ConditionalChangeExpression> parse(String query, ANTLRErrorListener errorListener) {
		CharStream charStream = CharStreams.fromString(query);

		TFLangLexer lexer = new TFLangLexer(charStream);
		lexer.removeErrorListeners();
		if (errorListener != null) {
			lexer.addErrorListener(errorListener);
		}

		TokenStream tokenStream = new CommonTokenStream(lexer);
		TFLangParser parser = new TFLangParser(tokenStream);
		parser.removeErrorListeners();
		if (errorListener != null) {
			parser.addErrorListener(errorListener);
		}

		ParseTree parseTree = parser.parseConditionalChangeExpressionMany();

		return new TfLangConditionalChangeExpressionParser().visit(parseTree);
	}


}
