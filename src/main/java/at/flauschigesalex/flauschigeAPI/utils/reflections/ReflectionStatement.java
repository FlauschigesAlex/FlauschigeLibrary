package at.flauschigesalex.flauschigeAPI.utils.reflections;

import org.reflections.Reflections;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public final class ReflectionStatement {

    private final String[] reflectionPaths;
    private ArrayList<Reflections> reflections;

    ArrayList<Reflections> getReflected() {
        if (this.reflections == null) {
            reflections = new ArrayList<>();
            for (String reflectionPath : reflectionPaths)
                reflections.add(new Reflections(reflectionPath));
        }
        return reflections;
    }

    ReflectionStatement(String reflectionPath, String... moreReflectionPaths) {
        final ArrayList<String> list = new ArrayList<>(List.of(reflectionPath));
        Collections.addAll(list, moreReflectionPaths);
        this.reflectionPaths = list.toArray(String[]::new);
    }

    private final ArrayList<Class<?>> ignoredClasses = new ArrayList<>();
    private final ArrayList<Class<?>> ignoredExtendedClasses = new ArrayList<>();
    private final ArrayList<Class<? extends Annotation>> ignoredAnnotations = new ArrayList<>();

    public ReflectionStatement ignoreClass(Class<?>... clazz) {
        for (Class<?> aClass : clazz) {
            if (ignoredClasses.contains(aClass)) continue;
            ignoredClasses.add(aClass);
        }
        return this;
    }

    private ArrayList<Class<?>> removeAll(ArrayList<Class<?>> arrayList) {
        for (Reflections reflected : getReflected()) {
            arrayList.removeAll(reflected.getTypesAnnotatedWith(ReflectorInvisible.class));
        }
        arrayList.removeAll(ignoredClasses);
        return arrayList;
    }

    public ArrayList<Class<?>> getSubClasses(Class<?> clazz) {
        ArrayList<Class<?>> reflectedClasses = new ArrayList<>();
        for (Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getSubTypesOf(clazz));
        }
        return removeAll(reflectedClasses);
    }

    public ArrayList<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotation) {
        ArrayList<Class<?>> reflectedClasses = new ArrayList<>();
        for (Reflections reflected : getReflected()) {
            reflectedClasses.addAll(reflected.getTypesAnnotatedWith(annotation));
        }
        return removeAll(reflectedClasses);
    }
}
