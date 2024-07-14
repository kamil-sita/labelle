package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.action.TciAction;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.TciScope;
import place.sita.tflang.ConditionalChangeExpression;
import place.sita.tflang.parsers.StringToMultipleConditionalChangeExpressionParser;

import java.util.List;

public class TfLangChangeExpressionToTciInstructionConverter {

	public static List<TciInstruction> convert(String expression) {
		return convert(StringToMultipleConditionalChangeExpressionParser.parse(expression));
	}

	public static List<TciInstruction> convert(List<ConditionalChangeExpression> expressions) {
		return expressions.stream()
			.map(TfLangChangeExpressionToTciInstructionConverter::convert)
			.toList();
	}

	public static TciInstruction convert(ConditionalChangeExpression expression) {
		TciScope scope = new TFlangFilteringToTciScope().visit(expression.filter());
		List<TciAction> actions = new TFlangChangeToTciAction().visit(expression.change());

		return new TciInstruction(scope, actions);
	}

}
