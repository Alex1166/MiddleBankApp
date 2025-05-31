package my.bankapp.exception;

public class IdentifierNotProvidedException extends RuntimeException {
    public IdentifierNotProvidedException(String message) {
        super(message);
    }

    public IdentifierNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentifierNotProvidedException(Throwable cause) {
        super(cause);
    }
}
