package at.flauschigesalex.defaultLibrary.utils.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public abstract class ProjectManager {

    public static Comparator<ProjectManager> comparator() {
        return new ProjectManagerComparator();
    }

    protected final ArrayList<ProjectManagerPredicate<? extends Class<?>>> predicates = new ArrayList<>();
    protected ProjectManager(ProjectManagerPredicate<?>... array) {
        predicates.addAll(List.of(array));
    }

    protected double priority() {
        return 1.0;
    }

    protected abstract void execute();

    public final boolean matches() {
        for (ProjectManagerPredicate<? extends Class<?>> predicate : predicates) {
            if (!predicate.matches())
                return false;
        }
        return true;
    }

    public final void executeManager() {
        if (!this.matches())
            return;
        this.execute();
    }
}

