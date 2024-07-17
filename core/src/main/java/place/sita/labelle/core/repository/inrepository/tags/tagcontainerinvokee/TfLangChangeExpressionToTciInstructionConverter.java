package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import org.antlr.v4.runtime.ANTLRErrorListener;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.action.TciAction;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.TciScope;
import place.sita.tflang.ConditionalChangeExpression;
import place.sita.tflang.parsers.StringToMultipleConditionalChangeExpressionParser;

import java.util.List;
import java.util.Objects;

public class TfLangChangeExpressionToTciInstructionConverter {

	public static List<TciInstruction> convert(String expression) {
		return convert(expression, null);
	}

	public static List<TciInstruction> convert(String expression, ANTLRErrorListener errorListener) {
		return convert(StringToMultipleConditionalChangeExpressionParser.parse(expression, errorListener));
	}

	public static List<TciInstruction> convert(List<ConditionalChangeExpression> expressions) {
		return expressions.stream()
			.map(TfLangChangeExpressionToTciInstructionConverter::convert)
			.toList();
	}

	public static TciInstruction convert(ConditionalChangeExpression expression) {
		Objects.requireNonNull(expression, "expression cannot be null");
		Objects.requireNonNull(expression.filter(), "expression.filter() cannot be null");
		Objects.requireNonNull(expression.change(), "expression.change() cannot be null");
		TciScope scope = new TFlangFilteringToTciScope().visit(expression.filter());
		List<TciAction> actions = new TFlangChangeToTciAction().visit(expression.change());

		return new TciInstruction(scope, actions);
	}

}
