package at.flauschigesalex.defaultLibrary.utils;

import org.jetbrains.annotations.Nullable;

/**
 * {@link Exception General-Exception} regarding the {@link at.flauschigesalex.defaultLibrary.FlauschigeLibrary project} or its children.
 */
@SuppressWarnings("unused")
public class LibraryException extends RuntimeException {

    protected LibraryException() {
        super();
    }

    public LibraryException(final @Nullable String message) {
        super(message);
    }

    protected LibraryException(final @Nullable String message, Throwable cause) {
        super(message, cause);
    }

    protected LibraryException(final @Nullable Throwable cause) {
        super(cause);
    }
}
