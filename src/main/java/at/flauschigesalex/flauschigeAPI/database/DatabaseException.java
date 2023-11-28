package at.flauschigesalex.flauschigeAPI.database;

import at.flauschigesalex.flauschigeAPI.exception.APIException;

public abstract class DatabaseException extends APIException {

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
