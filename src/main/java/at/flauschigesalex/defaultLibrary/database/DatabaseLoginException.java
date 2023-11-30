package at.flauschigesalex.defaultLibrary.database;

@SuppressWarnings("unused")
public final class DatabaseLoginException extends DatabaseException {

    DatabaseLoginException() {
        super();
    }

    public DatabaseLoginException(String message) {
        super(message);
    }

    DatabaseLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    DatabaseLoginException(Throwable cause) {
        super(cause);
    }
}
