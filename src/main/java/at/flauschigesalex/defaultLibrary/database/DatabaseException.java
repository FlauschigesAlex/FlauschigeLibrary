package at.flauschigesalex.defaultLibrary.database;

import at.flauschigesalex.defaultLibrary.exception.LibraryException;

public abstract class DatabaseException extends LibraryException {

    protected DatabaseException() {
        super();
    }

    protected DatabaseException(String message) {
        super(message);
    }

    protected DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    protected DatabaseException(Throwable cause) {
        super(cause);
    }
}
