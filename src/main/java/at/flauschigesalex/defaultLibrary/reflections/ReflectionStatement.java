package at.flauschigesalex.defaultLibrary.reflections;

import at.flauschigesalex.defaultLibrary.utils.Invisible;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.*;

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

    public <C> List<Class<? extends C>> getSubClasses(final @NotNull Class<C> clazz) {
        final HashSet<Class<? extends C>> reflected = new HashSet<>();
        for (final Reflections reflections : getReflected())
            reflected.addAll(reflections.getSubTypesOf(clazz));
        return removeUnnecessary(new HashSet<>(reflected));
    }

    public <A extends Annotation> List<Class<?>> getAnnotatedClasses(final @NotNull Class<A> annotation) {
        final HashSet<Class<?>> reflected = new HashSet<>();
        for (final Reflections reflections : getReflected())
            reflected.addAll(reflections.getTypesAnnotatedWith(annotation));
        return removeUnnecessary(reflected);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private <C> List<Class<? extends C>> removeUnnecessary(final @NotNull HashSet<Class<? extends C>> set) {
        set.removeIf(aClass -> aClass.isAnnotationPresent(Invisible.class));
        ignoredClasses.forEach(set::remove);
        return new ArrayList<>(set);
    }

    ArrayList<Reflections> getReflected() {
        if (this.reflections != null)
            return this.reflections;

        this.reflections = new ArrayList<>();
        for (final String reflectionPath : reflectionPaths) {
            if (reflectionPath == null || reflectionPath.isEmpty() || reflectionPath.isBlank())
                continue;

            this.reflections.add(new Reflections(reflectionPath));
        }
        return this.reflections;
    }
}
