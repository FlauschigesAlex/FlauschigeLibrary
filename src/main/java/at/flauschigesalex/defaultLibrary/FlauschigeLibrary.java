package at.flauschigesalex.defaultLibrary;

import at.flauschigesalex.defaultLibrary.minecraft.api.MojangAPI;
import at.flauschigesalex.defaultLibrary.utils.managers.ProjectManager;
import at.flauschigesalex.defaultLibrary.utils.reflections.Reflector;
import lombok.Getter;
import java.util.ArrayList;

@SuppressWarnings({"unused"})
@Getter
public class FlauschigeLibrary {
    private static FlauschigeLibrary flauschigeLibrary;

    public static void main(String[] args) {
        getLibrary();
    }

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     */
    public static FlauschigeLibrary getLibrary() {
        if (flauschigeLibrary == null) flauschigeLibrary = new FlauschigeLibrary();
        return flauschigeLibrary;
    }

    private final String ownDirectoryPath;
    private final ArrayList<String> workingDirectoryPath = new ArrayList<>();

    protected FlauschigeLibrary() {
        this.ownDirectoryPath = getClass().getPackage().getName();
        loop:
        for (Package definedPackage : getClass().getClassLoader().getDefinedPackages()) {
            if (definedPackage.getName().startsWith(ownDirectoryPath)) continue;
            for (String workingDirectory : this.workingDirectoryPath) {
                if (definedPackage.getName().startsWith(workingDirectory)) continue loop;
            }
            this.workingDirectoryPath.add(definedPackage.getName());
        }

        final ArrayList<ProjectManager> managers = new ArrayList<>();
        for (Class<? extends ProjectManager> subClass : getReflector().reflect(ownDirectoryPath, workingDirectoryPath.toArray(String[]::new)).getSubClasses(ProjectManager.class)) {
            try {
                managers.add(subClass.getConstructor().newInstance());
            } catch (Exception ignore) {
            }
        }
        managers.sort(ProjectManager.comparator());
        managers.forEach(manager -> manager.executeManager(this));
    }

    public FlauschigeLibrary addWorkingDirectory(String path) {
        this.workingDirectoryPath.add(path);
        return this;
    }

    public MojangAPI getMojangAPI() {
        return MojangAPI.mojangAPI();
    }

    public Reflector getReflector() {
        return Reflector.getReflector();
    }
}
