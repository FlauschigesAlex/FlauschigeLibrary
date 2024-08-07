package at.flauschigesalex.defaultLibrary.project;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

@SuppressWarnings({"unused", "unchecked"})
public abstract class ProjectManager<V> {

    private static final ArrayList<ProjectManager<?>> managers = new ArrayList<>();

    public static Comparator<ProjectManager<?>> comparator() {
        return Comparator.comparingDouble(ProjectManager::priority);
    }

    public static <V extends ProjectManager<?>> @Nullable V byName(final @NotNull String name, boolean ignoreCase) {
        for (final ProjectManager<?> manager : managers) {
            if (ignoreCase && manager.getClass().getSimpleName().equalsIgnoreCase(name))
                try {
                    return (V) manager;
                } catch (Exception ignore) {
                }
            if (manager.getClass().getSimpleName().equals(name))
                try {
                    return (V) manager;
                } catch (Exception ignore) {
                }
        }
        return null;
    }

    public static <V extends ProjectManager<?>> @Nullable V byClass(final @NotNull Class<? extends ProjectManager<?>> source) {
        for (final ProjectManager<?> manager : managers) {
            if (manager.getClass() == source)
                try {
                    return (V) manager;
                } catch (Exception ignore) {
                }
        }
        return null;
    }

    protected Predicate<V> predicate;
    protected V anything;
    private FlauschigeLibrary library;

    protected ProjectManager() {
        ProjectManager.managers.add(this);
    }

    protected ProjectManager(final @NotNull Predicate<V> predicate, final @Nullable V anything) {
        this.predicate = predicate;
        this.anything = anything;
        ProjectManager.managers.add(this);
    }

    protected double priority() {
        return 1.0;
    }

    public String successMessage() {
        return null;
    }

    public String predicateFailMessage() {
        return null;
    }

    public String failMessage() {
        return null;
    }

    protected abstract void execute();

    public final boolean matches() {
        return predicate == null || predicate.test(anything);
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

        try {
            this.execute();
            return true;
        } catch (final Exception fail) {
            if (predicateFailMessage() != null)
                System.out.println(predicateFailMessage().replaceFirst("%s", Arrays.toString(fail.getStackTrace())));
        }
        return false;
    }
}

