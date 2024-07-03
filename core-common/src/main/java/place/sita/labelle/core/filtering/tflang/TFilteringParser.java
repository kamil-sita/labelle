package place.sita.labelle.core.filtering.tflang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jooq.Condition;
import org.jooq.Field;
import place.sita.labelle.tflang.TFLangLexer;
import place.sita.labelle.tflang.TFLangParser;

import java.util.HashMap;
import java.util.Map;

public class TFilteringParser {


    public static Condition condition(String query) {

        CharStream charStream = CharStreams.fromString(query);

        TFLangLexer lexer = new TFLangLexer(charStream);
        // todo error handling
        //lexer.removeErrorListeners();

        TokenStream tokenStream = new CommonTokenStream(lexer);
        TFLangParser parser = new TFLangParser(tokenStream);
        // todo error handling

        ParseTree parseTree = parser.parseMatchExpression();

        Map<String, Field<String>> fields = new HashMap<>();
        //fields.put("tag", )

        RootVisitor rootVisitor = new RootVisitor(fields);
        return rootVisitor.visit(parseTree);
    }

}
