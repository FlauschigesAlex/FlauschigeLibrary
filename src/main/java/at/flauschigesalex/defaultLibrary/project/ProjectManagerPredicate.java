package at.flauschigesalex.defaultLibrary.project;

@SuppressWarnings("unused")
public interface ProjectManagerPredicate<V> {
    boolean matches(V anything);
}
