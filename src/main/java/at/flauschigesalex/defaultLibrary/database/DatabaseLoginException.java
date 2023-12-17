package at.flauschigesalex.defaultLibrary.database;

@SuppressWarnings("unused")
public final class DatabaseLoginException extends DatabaseException {

    public DatabaseLoginException(String message) {
        super(message);
    }

    DatabaseLoginException() {
        super();
    }

    DatabaseLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    DatabaseLoginException(Throwable cause) {
        super(cause);
    }
}
