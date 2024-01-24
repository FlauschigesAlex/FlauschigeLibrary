package at.flauschigesalex.defaultLibrary.exception;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public abstract class LibraryException extends RuntimeException {

    protected LibraryException() {
        super();
    }

    protected LibraryException(final @Nullable String message) {
        super(message);
    }

    protected LibraryException(final @Nullable String message, Throwable cause) {
        super(message, cause);
    }

    protected LibraryException(final @Nullable Throwable cause) {
        super(cause);
    }
}
