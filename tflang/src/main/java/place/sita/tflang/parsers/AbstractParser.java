package place.sita.tflang.parsers;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import place.sita.tflang.TFLangLexer;
import place.sita.tflang.TFLangParser;

import java.util.function.Supplier;

public class AbstractParser {

	public static <U, T extends ParseTreeVisitor<U>> U parse(String query, Supplier<T> factory) {
		CharStream charStream = CharStreams.fromString(query);

		TFLangLexer lexer = new TFLangLexer(charStream);
		// todo error handling
		//lexer.removeErrorListeners();

		TokenStream tokenStream = new CommonTokenStream(lexer);
		TFLangParser parser = new TFLangParser(tokenStream);
		// todo error handling

		ParseTree parseTree = parser.parseMatchExpression();

		T t = factory.get();
		return t.visit(parseTree);
	}

}
