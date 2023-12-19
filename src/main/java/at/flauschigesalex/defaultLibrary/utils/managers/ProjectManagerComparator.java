package at.flauschigesalex.defaultLibrary.utils.managers;

import java.util.Comparator;

public class ProjectManagerComparator implements Comparator<ProjectManager> {

    ProjectManagerComparator() {
    }

    public int compare(final ProjectManager pm1, final ProjectManager pm2) {
        return Double.compare(pm1.priority(), pm2.priority());
    }
}
