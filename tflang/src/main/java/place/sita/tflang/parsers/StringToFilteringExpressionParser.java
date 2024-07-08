package place.sita.tflang.parsers;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import place.sita.tflang.TFLangLexer;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.parsing.TFlangFilteringExpressionParser;

public class StringToFilteringExpressionParser {

	public static FilteringExpression parse(String query) {
		CharStream charStream = CharStreams.fromString(query);

		TFLangLexer lexer = new TFLangLexer(charStream);
		// todo error handling
		//lexer.removeErrorListeners();

		TokenStream tokenStream = new CommonTokenStream(lexer);
		TFLangParser parser = new TFLangParser(tokenStream);
		// todo error handling

		ParseTree parseTree = parser.parseMatchExpression();

		return new TFlangFilteringExpressionParser().visit(parseTree);
	}

}
