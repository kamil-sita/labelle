package place.sita.tflang.filteringexpression;

import java.util.List;

public non-sealed interface InExpression extends FilteringExpression {

	String key();

	List<String> values();

}