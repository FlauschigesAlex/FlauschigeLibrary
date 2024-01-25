package at.flauschigesalex.defaultLibrary.utils;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"unchecked","unused"})
public abstract class AutoDisplayable {

    private boolean displayClassType = true;
    private boolean displayDeepStructure = true;
    private DisplayType displayType = DisplayType.DISPLAY_FIELD;

    protected final <P extends AutoDisplayable> P setDisplayClassType(boolean displayClassType) {
        this.displayClassType = displayClassType;
        return (P) this;
    }

    protected final <P extends AutoDisplayable> P setDisplayDeepStructure(boolean displayDeepStructure) {
        this.displayDeepStructure = displayDeepStructure;
        return (P) this;
    }

    /**
     * @deprecated There are currently no other {@link DisplayType display-types}.
     */
    protected final <P extends AutoDisplayable> P setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
        return (P) this;
    }

    public String toString() {
        Class<?> THIS = this.getClass();
        final HashMap<Class<?>, HashMap<Field, ?>> list = new HashMap<>();

        while (THIS != AutoDisplayable.class && THIS != Object.class && displayType.canDisplay(Field.class)) {
            final HashMap<Field, Object> map = new HashMap<>();
            for (final Field declaredField : THIS.getDeclaredFields()) {
                declaredField.setAccessible(true);
                if (Modifier.isStatic(declaredField.getModifiers())) continue;
                if (declaredField.isAnnotationPresent(Invisible.class)) continue;
                if ((THIS != this.getClass() || displayDeepStructure) && Modifier.isPrivate(declaredField.getModifiers()))
                    continue;

                try {
                    final Object value = declaredField.get(this);
                    map.put(declaredField, value == null ? "" : value);
                } catch (Exception fail) {
                    map.put(declaredField, "");
                }
            }

            list.put(THIS, map);
            THIS = THIS.getSuperclass();
        }

        final StringBuilder builder = new StringBuilder();
        final String spacer = "  ";
        final int[] tabSpammen = new int[]{0};
        list.forEach((type, map) -> {
            builder.append(spacer.repeat(tabSpammen[0])).append(!builder.isEmpty() ? "\n\n" + spacer + "Parent of " : "").append(type.getSimpleName()).append(" {");
            map.forEach((field, value) -> builder.append("\n").append(spacer.repeat(tabSpammen[0] + 1))
                    .append(displayClassType ? "(" + field.getType().getSimpleName() + ") " : "").append(field.getName()).append(": ").append(value));
            tabSpammen[0]++;
        });
        for (int spamman = tabSpammen[0]; spamman > 0; spamman--) {
            builder.append("\n").append(spacer.repeat(spamman - 1)).append("}");
        }

        return builder.toString();
    }

    public enum DisplayType {
        DISPLAY_FIELD(Field.class),
        ;

        private final ArrayList<Class<? extends AccessibleObject>> display;

        DisplayType(final @NotNull Class<? extends AccessibleObject>... display) {
            this(new ArrayList<>(List.of(display)));
        }

        DisplayType(final @NotNull ArrayList<Class<? extends AccessibleObject>> display) {
            this.display = new ArrayList<>(display);
        }

        @SuppressWarnings("SameParameterValue")
        boolean canDisplay(final @NotNull Class<? extends AccessibleObject> display) {
            return this.display.contains(display);
        }
    }
}
