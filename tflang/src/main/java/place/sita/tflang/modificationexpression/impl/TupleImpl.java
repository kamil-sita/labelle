package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.changeexpression.StringOrMatched;
import place.sita.tflang.modificationexpression.changeexpression.Tuple;

import java.util.List;

public record TupleImpl(List<StringOrMatched> v) implements Tuple {
	@Override
	public StringOrMatched valueAt(int index) {
		return v.get(index);
	}

	@Override
	public int dimensionality() {
		return v.size();
	}
}
