package at.flauschigesalex.defaultLibrary.database;

import at.flauschigesalex.defaultLibrary.exception.LibraryException;
import org.jetbrains.annotations.Nullable;

public abstract class DatabaseException extends LibraryException {

    protected DatabaseException() {
        super();
    }

    protected DatabaseException(final @Nullable String message) {
        super(message);
    }

    protected DatabaseException(final @Nullable String message, Throwable cause) {
        super(message, cause);
    }

    protected DatabaseException(final @Nullable Throwable cause) {
        super(cause);
    }
}
