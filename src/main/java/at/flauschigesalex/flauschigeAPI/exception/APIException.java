package at.flauschigesalex.flauschigeAPI.exception;

@SuppressWarnings("unused")
public abstract class APIException extends RuntimeException {

    protected APIException() {
        super();
    }

    protected APIException(String message) {
        super(message);
    }

    protected APIException(String message, Throwable cause) {
        super(message, cause);
    }

    protected APIException(Throwable cause) {
        super(cause);
    }
}
