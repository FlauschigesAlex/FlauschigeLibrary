package at.flauschigesalex.defaultLibrary.execution.manager;

import org.jetbrains.annotations.NotNull;
import java.util.Comparator;

/**
 * A {@link Comparator comparator} used to determine which {@link ProjectManager manager} is prioritized next.
 * @see ProjectManager#priority() Priority
 */
public class ProjectManagerComparator implements Comparator<ProjectManager> {

    ProjectManagerComparator() {
    }

    public int compare(final @NotNull ProjectManager pm1, final @NotNull ProjectManager pm2) {
        return Double.compare(pm1.priority(), pm2.priority());
    }
}
