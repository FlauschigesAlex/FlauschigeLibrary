package at.flauschigesalex.defaultLibrary.utils.managers;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import org.jetbrains.annotations.NotNull;
import java.util.Comparator;

@SuppressWarnings("unused")
public abstract class ProjectManager {

    public static Comparator<ProjectManager> comparator() {
        return new ProjectManagerComparator();
    }

    protected ProjectManagerPredicate predicate;

    protected ProjectManager(ProjectManagerPredicate predicate) {
        this.predicate = predicate;
    }

    protected double priority() {
        return 1.0;
    }

    protected abstract void execute();

    public final boolean matches() {
        return predicate.matches();
    }

    private FlauschigeLibrary library;

    protected final FlauschigeLibrary parentLibrary() {
        return library;
    }

    public final void executeManager(final @NotNull FlauschigeLibrary library) {
        this.library = library;
        if (!this.matches())
            return;
        this.execute();
    }
}

