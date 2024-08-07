package at.flauschigesalex.defaultLibrary;

import at.flauschigesalex.defaultLibrary.minecraft.api.MojangAPI;
import at.flauschigesalex.defaultLibrary.project.ProjectManager;
import at.flauschigesalex.defaultLibrary.project.task.Task;
import at.flauschigesalex.defaultLibrary.reflections.Reflector;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;

@SuppressWarnings({"unused"})
@Getter
public class FlauschigeLibrary {
    protected static boolean autoRegisterManagers = true;
    private static FlauschigeLibrary flauschigeLibrary;

    private final String ownDirectoryPath;
    private final ArrayList<String> workingDirectoryPath = new ArrayList<>();

    protected FlauschigeLibrary() {
        this.ownDirectoryPath = getClass().getPackage().getName();
        loop:
        for (final Package definedPackage : getClass().getClassLoader().getDefinedPackages()) {
            if (definedPackage.getName().startsWith(ownDirectoryPath))
                continue;

            for (final String workingDirectory : this.workingDirectoryPath)
                if (definedPackage.getName().startsWith(workingDirectory))
                    continue loop;

            this.workingDirectoryPath.add(definedPackage.getName());
        }

        if (autoRegisterManagers)
            executeManagers();
    }

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
        if (flauschigeLibrary == null)
            flauschigeLibrary = new FlauschigeLibrary();
        return flauschigeLibrary;
    }

    /**
     * Make sure to run this method in your main class!
     * This is extremely important for reflections!
     *
     * @return an instance of the Library
     */
    public static FlauschigeLibrary getLibrary(final boolean autoRegisterManagers) {
        FlauschigeLibrary.autoRegisterManagers = autoRegisterManagers;
        return getLibrary();
    }

    @SuppressWarnings("rawtypes")
    public void executeManagers() {
        final ArrayList<ProjectManager<?>> managers = new ArrayList<>();
        for (final Class<? extends ProjectManager> subClass : getReflector().reflect(ownDirectoryPath, workingDirectoryPath.toArray(String[]::new)).getSubClasses(ProjectManager.class)) {
            try {
                final Constructor<?> constructor = subClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                managers.add((ProjectManager<?>) constructor.newInstance());
            } catch (Exception ignore) {
            }
        }
        managers.sort(ProjectManager.comparator().reversed());

        Task.createAsyncTask((legacyTask) -> managers.forEach(manager -> {
            try {
                if (manager.executeManager(this)) {
                    if (manager.successMessage() != null)
                        System.out.println(manager.successMessage());
                } else {
                    if (manager.failMessage() != null)
                        System.out.println(manager.failMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).execute();
    }

    public FlauschigeLibrary addWorkingDirectory(final @NotNull String path) {
        this.workingDirectoryPath.add(path);
        return this;
    }

    public MojangAPI getMojangAPI() {
        return MojangAPI.mojangAPI();
    }

    public Reflector getReflector() {
        return Reflector.getReflector();
    }

    public ArrayList<String> getWorkingDirectoryPath() {
        return new ArrayList<>(workingDirectoryPath);
    }
}
