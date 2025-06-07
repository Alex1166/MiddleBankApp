package my.bankapp.exception;

public class InvalidMoneyValueException extends RuntimeException {
    public InvalidMoneyValueException(String message) {
        super(message);
    }

    public InvalidMoneyValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMoneyValueException(Throwable cause) {
        super(cause);
    }
}
