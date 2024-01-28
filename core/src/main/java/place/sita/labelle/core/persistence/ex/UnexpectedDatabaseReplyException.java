package place.sita.labelle.core.persistence.ex;

public class UnexpectedDatabaseReplyException extends RuntimeException {

    public UnexpectedDatabaseReplyException() {
        super();
    }

    public UnexpectedDatabaseReplyException(String message) {
        super(message);
    }
}
