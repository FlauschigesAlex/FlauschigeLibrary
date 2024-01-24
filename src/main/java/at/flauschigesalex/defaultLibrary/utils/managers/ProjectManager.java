package at.flauschigesalex.defaultLibrary.utils.managers;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import org.jetbrains.annotations.NotNull;
import java.util.Comparator;

@SuppressWarnings("unused")
public abstract class ProjectManager {

    protected ProjectManagerPredicate predicate;
    private FlauschigeLibrary library;

    protected ProjectManager() {
    }

    protected ProjectManager(final @NotNull ProjectManagerPredicate predicate) {
        this.predicate = predicate;
    }

    public static Comparator<ProjectManager> comparator() {
        return new ProjectManagerComparator();
    }

    protected double priority() {
        return 1.0;
    }

    public String successMessage() {
        return null;
    }

    public String predicateFailMessage() {
        return null;
    }

    public String failMessage() {
        return null;
    }

    protected abstract boolean execute();

    public final boolean matches() {
        return predicate == null || predicate.matches();
    }

    protected final FlauschigeLibrary parentLibrary() {
        return library;
    }

    public final boolean executeManager(final @NotNull FlauschigeLibrary library) {
        this.library = library;
        if (!this.matches()) {
            if (predicateFailMessage() != null)
                System.out.println(predicateFailMessage());
            return false;
        }
        return this.execute();
    }
}

