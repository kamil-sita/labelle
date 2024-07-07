package place.sita.tflang.modificationexpression;

public interface Tuple {

	StringOrMatched valueAt(int index);

	int dimensionality();

}
