package at.flauschigesalex.defaultLibrary.utils.reflections;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import lombok.Getter;
import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@SuppressWarnings({"unused", "UnusedReturnValue"})
@ReflectorInvisible
public final class Reflector {

    private static Reflector reflector;

    public static Reflector getReflector() {
        if (reflector == null) reflector = new Reflector();
        return reflector;
    }

    private final HashMap<String[], ReflectionStatement> cache = new HashMap<>();

    private Reflector() {
    }

    private String reflectionPath;

    @CheckReturnValue
    public ReflectionStatement reflect() {
        final ArrayList<String> reflectMe = new ArrayList<>(List.of(FlauschigeLibrary.getAPI().getOwnDirectoryPath()));
        reflectMe.addAll(FlauschigeLibrary.getAPI().getWorkingDirectoryPath());
        final String[] reflectionPaths = reflectMe.toArray(String[]::new);
        if (!cache.containsKey(reflectionPaths)) cache.put(reflectionPaths, new ReflectionStatement(reflectionPath));
        return cache.get(reflectionPaths);
    }

    @CheckReturnValue
    public ReflectionStatement reflect(String reflectionPath) {
        if (!cache.containsKey(new String[]{reflectionPath})) cache.put(new String[]{reflectionPath}, new ReflectionStatement(reflectionPath));
        return cache.get(new String[]{reflectionPath});
    }
}

