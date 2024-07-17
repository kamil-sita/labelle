package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory;

import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.TFlangFilteringToTciScope;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope.TciScope;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.parsers.StringToFilteringExpressionParser;

public class StringToTagFilter {

	public static TagFiltering fromString(String filter) {
		FilteringExpression fe = StringToFilteringExpressionParser.parse(filter);
		TFlangFilteringToTciScope tflangFilteringToTciScope = new TFlangFilteringToTciScope();
		TciScope scope = tflangFilteringToTciScope.visit(fe);
		TciScopeToTagFilterConverter converter = new TciScopeToTagFilterConverter();
		return converter.visit(scope);
	}

}
