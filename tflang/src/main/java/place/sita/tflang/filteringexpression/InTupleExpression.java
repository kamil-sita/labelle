package place.sita.tflang.filteringexpression;

import java.util.List;

public non-sealed interface InTupleExpression extends FilteringExpression {
	List<String> keys();

	List<List<String>> values();
}
