package place.sita.tflang.filteringexpression;

import java.util.List;

public non-sealed interface OrExpression extends FilteringExpression {

	List<FilteringExpression> expressions();
}
