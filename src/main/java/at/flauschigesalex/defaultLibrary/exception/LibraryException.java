package at.flauschigesalex.defaultLibrary.exception;

@SuppressWarnings("unused")
public abstract class LibraryException extends RuntimeException {

    protected LibraryException() {
        super();
    }

    protected LibraryException(String message) {
        super(message);
    }

    protected LibraryException(String message, Throwable cause) {
        super(message, cause);
    }

    protected LibraryException(Throwable cause) {
        super(cause);
    }
}
