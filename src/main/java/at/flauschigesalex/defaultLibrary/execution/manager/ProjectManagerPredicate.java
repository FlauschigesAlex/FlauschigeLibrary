package at.flauschigesalex.defaultLibrary.execution.manager;

@SuppressWarnings("unused")
public interface ProjectManagerPredicate<V> {
    boolean matches(V anything);
}
