package place.sita.tflang.parsers;

import place.sita.tflang.filteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.parsing.FilteringExpressionParser;

public class StringToFilteringExpressionParser {

	public static FilteringExpression parse(String query) {
		return AbstractParser.parse(query, () -> new FilteringExpressionParser());
	}

}
