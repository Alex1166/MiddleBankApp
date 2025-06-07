package my.bankapp.exception;

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }

  public TransactionException(Throwable cause) {
    super(cause);
  }

  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}
