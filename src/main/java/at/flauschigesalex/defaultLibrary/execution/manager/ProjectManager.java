package at.flauschigesalex.defaultLibrary.execution.manager;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import java.util.Comparator;

/**
 * Called after initialization of the {@link FlauschigeLibrary library}.<br>
 * These classes are used to execute tasks ordered without having to overcrowd the main class.<br>
 * {@link #execute() Execution} may be done {@link FlauschigeLibrary#executeManagers() manually} instead of {@link FlauschigeLibrary#executeManagers() automatically}.
 */
@SuppressWarnings("unused")
public abstract class ProjectManager {

    protected ProjectManagerPredicate predicate;
    private FlauschigeLibrary library;

    /**
     * Creates an instance without a {@link ProjectManagerPredicate predicate}.
     */
    protected ProjectManager() {
    }

    /**
     * Creates an instance with a {@link ProjectManagerPredicate predicate}.
     * @param predicate Requirements to {@link #execute() execute} this manager
     * @see ProjectManagerPredicate Predicate
     */
    protected ProjectManager(final @NotNull ProjectManagerPredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * @return An instance of a {@link ProjectManager manager-comparator}.
     * @see ProjectManagerComparator
     * @see Comparator
     */
    public static Comparator<ProjectManager> comparator() {
        return new ProjectManagerComparator();
    }

    /**
     * The priority of the manager used to determine the {@link #execute() execution} sequence.
     */
    protected @Range(from = 1, to = Integer.MAX_VALUE) double priority() {
        return 1.0;
    }

    /**
     * Message {@link java.io.PrintStream#println(String) printed} if the manager is executed successfully.
     */
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

