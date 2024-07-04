package place.sita.tflang.filteringexpression;

import java.util.List;

public non-sealed interface AndExpression extends FilteringExpression {

	List<FilteringExpression> expressions();

}
