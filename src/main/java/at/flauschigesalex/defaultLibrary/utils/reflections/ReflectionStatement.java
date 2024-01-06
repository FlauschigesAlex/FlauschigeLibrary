package at.flauschigesalex.defaultLibrary.utils.reflections;

import at.flauschigesalex.defaultLibrary.utils.Invisible;
import at.flauschigesalex.defaultLibrary.utils.Printable;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings({"unused"})
public final class ReflectionStatement extends Printable {

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

    public <C> ArrayList<Class<? extends C>> getSubClasses(final @NotNull Class<C> clazz) {
        final ArrayList<Class<? extends C>> reflectedClasses = new ArrayList<>();
        for (final Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getSubTypesOf(clazz));
        }
        return removeUnnecessary(reflectedClasses);
    }

    public <A extends Annotation> ArrayList<Class<?>> getAnnotatedClasses(final @NotNull Class<A> annotation) {
        final ArrayList<Class<?>> reflectedClasses = new ArrayList<>();
        for (final Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getTypesAnnotatedWith(annotation));
        }
        return removeUnnecessary(reflectedClasses);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private <C> ArrayList<Class<? extends C>> removeUnnecessary(final @NotNull ArrayList<Class<? extends C>> arrayList) {
        arrayList.removeIf(aClass -> aClass.isAnnotationPresent(Invisible.class));
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
