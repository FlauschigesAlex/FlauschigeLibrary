package at.flauschigesalex.defaultLibrary.databases;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class DatabaseLoginException extends DatabaseException {

    public DatabaseLoginException(final @Nullable String message) {
        super(message);
    }

    DatabaseLoginException() {
        super();
    }

    DatabaseLoginException(final @Nullable String message, Throwable cause) {
        super(message, cause);
    }

    DatabaseLoginException(final @Nullable Throwable cause) {
        super(cause);
    }
}
