package at.flauschigesalex.defaultLibrary.utils.managers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class ProjectManagerPredicate<T extends Class<?>> {

    public static <T extends Class<?>> ProjectManagerPredicate<T> check(final @NotNull T classInstance, final @NotNull String fieldName) {
        return check(classInstance, fieldName, "true");
    }
    public static <T extends Class<?>> ProjectManagerPredicate<T> check(final @NotNull T classInstance, final @NotNull String fieldName, final @Nullable Object fieldValue) {
        return new ProjectManagerPredicate<>(classInstance, fieldName, fieldValue);
    }

    private final T classInstance;
    private final String fieldName;
    private final Object fieldValue;

    private ProjectManagerPredicate(final @NotNull T classInstance, final @NotNull String fieldName, final @Nullable Object fieldValue) {
        this.classInstance = classInstance;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    boolean matches() {
        try {
            final Object value = classInstance.getDeclaredField(fieldName).get(fieldValue);
            if (value == null || fieldValue == null)
                return value == null && fieldValue == null;
            return value.toString().equals(fieldValue.toString());
        } catch (Exception ignore) {
        }
        return fieldValue == null;
    }
}
