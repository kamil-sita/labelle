package place.sita.tflang;

public class TFlangUtil {

	public static String stripQuotes(String input) {
		if (input.startsWith("\"") && input.endsWith("\"")) {
			return input.substring(1, input.length() - 1);
		}
		return input;
	}

}
