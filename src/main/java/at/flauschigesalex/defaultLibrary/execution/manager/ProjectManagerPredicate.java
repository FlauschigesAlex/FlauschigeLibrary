package at.flauschigesalex.defaultLibrary.execution.manager;

/**
 * {@link ProjectManager ProjectManagers} are required to fulfill a given predicate before {@link ProjectManager#execute() executing a task}.
 */
@SuppressWarnings("unused")
public interface ProjectManagerPredicate {
    /**
     * A {@link Boolean value} that must be {@link Boolean#TRUE true} in order to {@link ProjectManager#execute() execute a task}.
     */
    boolean matches();
}
