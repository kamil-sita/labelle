package place.sita.modulefx;

public class BadApiUsageException extends RuntimeException {

	public BadApiUsageException() {
	}

	public BadApiUsageException(String message) {
		super(message);
	}

	public BadApiUsageException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadApiUsageException(Throwable cause) {
		super(cause);
	}
}
