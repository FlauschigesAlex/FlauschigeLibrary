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
    private final HashMap<ArrayList<String>, ReflectionStatement> cache = new HashMap<>();
    private String reflectionPath;

    private Reflector() {
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
    public ReflectionStatement reflect(String reflectionPath) {
        final ArrayList<String> list = new ArrayList<>(List.of(reflectionPath));
        if (!cache.containsKey(list)) cache.put(list, new ReflectionStatement(reflectionPath));
        return cache.get(list);
    }
}

