package at.flauschigesalex.defaultLibrary.project.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class TaskDelayType {

    private static final List<TaskDelayType> types = new ArrayList<>();

    public static TaskDelayType[] values() {
        return types.toArray(TaskDelayType[]::new);
    }

    public static @Nullable TaskDelayType valueOf(final @NotNull String name) {
        for (final TaskDelayType type : values())
            if (type.name.equalsIgnoreCase(name))
                return type;
        return null;
    }

    public static TaskDelayType defaultType() {
        return ONLY_BETWEEN;
    }

    public static final TaskDelayType ALWAYS, ONLY_BEGINNING, ONLY_BETWEEN;

    static {
        ALWAYS = new TaskDelayType("ALWAYS");
        ONLY_BEGINNING = new TaskDelayType("ONLY_BEGINNING");
        ONLY_BETWEEN = new TaskDelayType("ONLY_BETWEEN");
    }

    private final String name;

    private TaskDelayType(final @NotNull String name) {
        this.name = name;

        types.add(this);
    }

    public String name() {
        return name.toUpperCase();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TaskDelayType delayType)
            return delayType.name.equalsIgnoreCase(this.name);
        return false;
    }

    public String toString() {
        return name();
    }
}
