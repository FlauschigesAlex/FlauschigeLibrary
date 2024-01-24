package at.flauschigesalex.defaultLibrary.utils.managers;

import org.jetbrains.annotations.NotNull;
import java.util.Comparator;

public class ProjectManagerComparator implements Comparator<ProjectManager> {

    ProjectManagerComparator() {
    }

    public int compare(final @NotNull ProjectManager pm1, final @NotNull ProjectManager pm2) {
        return Double.compare(pm1.priority(), pm2.priority());
    }
}
