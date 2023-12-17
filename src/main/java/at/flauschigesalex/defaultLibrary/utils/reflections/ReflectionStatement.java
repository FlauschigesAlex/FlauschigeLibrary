package at.flauschigesalex.defaultLibrary.utils.reflections;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings({"unused"})
public final class ReflectionStatement {

    private final String[] reflectionPaths;
    private final ArrayList<Class<?>> ignoredClasses = new ArrayList<>();
    private final ArrayList<Class<?>> ignoredExtendedClasses = new ArrayList<>();
    private final ArrayList<Class<? extends Annotation>> ignoredAnnotations = new ArrayList<>();
    private ArrayList<Reflections> reflections;

    ReflectionStatement(final @NotNull String reflectionPath, final @NotNull String... moreReflectionPaths) {
        final ArrayList<String> list = new ArrayList<>();
        list.add(reflectionPath);
        Collections.addAll(list, moreReflectionPaths);
        this.reflectionPaths = list.toArray(String[]::new);
    }

    public ReflectionStatement ignoreClass(final @NotNull Class<?>... clazz) {
        for (final Class<?> aClass : clazz) {
            if (ignoredClasses.contains(aClass)) continue;
            ignoredClasses.add(aClass);
        }
        return this;
    }

    public ArrayList<Class<?>> getSubClasses(final @NotNull Class<?> clazz) {
        final ArrayList<Class<?>> reflectedClasses = new ArrayList<>();
        for (final Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getSubTypesOf(clazz));
        }
        return removeAll(reflectedClasses);
    }

    public ArrayList<Class<?>> getAnnotatedClasses(final @NotNull Class<? extends Annotation> annotation) {
        final ArrayList<Class<?>> reflectedClasses = new ArrayList<>();
        for (final Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getTypesAnnotatedWith(annotation));
        }
        return removeAll(reflectedClasses);
    }

    private ArrayList<Class<?>> removeAll(final @NotNull ArrayList<Class<?>> arrayList) {
        for (final Reflections reflected : getReflected()) {
            arrayList.removeAll(reflected.getTypesAnnotatedWith(ReflectorInvisible.class));
        }
        arrayList.removeAll(ignoredClasses);
        return arrayList;
    }

    ArrayList<Reflections> getReflected() {
        if (this.reflections == null) {
            reflections = new ArrayList<>();
            for (final String reflectionPath : reflectionPaths) {
                if (reflectionPath == null) continue;
                reflections.add(new Reflections(reflectionPath));
            }
        }
        return reflections;
    }
}
