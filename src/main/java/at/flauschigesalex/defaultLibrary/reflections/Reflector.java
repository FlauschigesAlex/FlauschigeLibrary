package at.flauschigesalex.defaultLibrary.reflections;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.utils.Invisible;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Invisible
public final class Reflector {

    private static Reflector reflector;
    private final HashMap<ArrayList<String>, ReflectionStatement> cache = new HashMap<>();
    private String reflectionPath;

    private Reflector() {
    }

    public static Reflector getReflector() {
        if (reflector == null) reflector = new Reflector();
        return reflector;
    }

    @CheckReturnValue
    public ReflectionStatement reflect() {
        final ArrayList<String> reflectMe = new ArrayList<>(List.of(FlauschigeLibrary.getLibrary().getOwnDirectoryPath()));
        reflectMe.addAll(FlauschigeLibrary.getLibrary().getWorkingDirectoryPath());
        if (!cache.containsKey(reflectMe))
            cache.put(reflectMe, new ReflectionStatement(FlauschigeLibrary.getLibrary().getOwnDirectoryPath(), FlauschigeLibrary.getLibrary().getWorkingDirectoryPath().toArray(String[]::new)));
        return cache.get(reflectMe);
    }

    @CheckReturnValue
    public ReflectionStatement reflect(final @NotNull String reflectionPath, final @NotNull String... moreReflectionPaths) {
        final ArrayList<String> list = new ArrayList<>(List.of(reflectionPath));
        list.addAll(List.of(moreReflectionPaths));
        if (!cache.containsKey(list)) cache.put(list, new ReflectionStatement(reflectionPath, moreReflectionPaths));
        return cache.get(list);
    }
}

