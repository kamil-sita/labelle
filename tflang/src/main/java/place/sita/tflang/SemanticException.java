package place.sita.tflang;

public class SemanticException extends RuntimeException {
	public SemanticException() {
	}

	public SemanticException(String message) {
		super(message);
	}

	public SemanticException(String message, Throwable cause) {
		super(message, cause);
	}

	public SemanticException(Throwable cause) {
		super(cause);
	}
}
