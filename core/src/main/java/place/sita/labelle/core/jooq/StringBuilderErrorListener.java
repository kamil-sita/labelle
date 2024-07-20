package place.sita.labelle.core.jooq;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class StringBuilderErrorListener extends BaseErrorListener {

	private final StringBuilder errors;

	public StringBuilderErrorListener(StringBuilder errors) {
		this.errors = errors;
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		errors.append("line ").append(line).append(":").append(charPositionInLine).append(" ").append(msg).append("\r\n");
	}
}
