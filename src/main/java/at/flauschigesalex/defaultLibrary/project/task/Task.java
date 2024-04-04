package at.flauschigesalex.defaultLibrary.project.task;

import at.flauschigesalex.defaultLibrary.time.TimeHandlerUnit;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

@SuppressWarnings({"unused"})
public final class Task {

    private static @Getter long totalTaskCount = 0;
    //private static final int default_check_amount = 4;
    private static final int default_delay_ms = 250;

    public static @CheckReturnValue Task createTask(final @NotNull Consumer<Task> consumer) {
        return new Task(consumer, false);
    }

    public static @CheckReturnValue Task createAsyncTask(final @NotNull Consumer<Task> consumer) {
        return new Task(consumer, true);
    }

    private final Consumer<Task> consumer;
    private final boolean async;

    private Thread thread;
    private Boolean repeating;

    //MAYBE SOMEDAY
    //private @Getter(AccessLevel.NONE) int checkAmount = default_check_amount;
    //private final ArrayList<Consumer<@NotNull Boolean>> stopConditions = new ArrayList<>();

    private Task(final @NotNull Consumer<Task> consumer, final boolean async) {
        this.consumer = consumer;
        this.async = async;
    }

    public void execute() {
        this.execute(1L, null, 0, null, null);
    }

    public void executeDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.execute(1L, unit, value, TaskDelayType.defaultType(), null);
    }

    public void executeDelayed(final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.execute(1L, unit, value, type, null);
    }

    public void repeat() {
        this.execute(null, null, 0, null, null);
    }

    public void repeat(final long amount) {
        this.execute(amount, null, 0, null, null);
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.execute(null, unit, value, TaskDelayType.defaultType(), null);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value) {
        this.execute(amount, unit, value, TaskDelayType.defaultType(), null);
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.execute(null, unit, value, type, null);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.execute(amount, unit, value, type, null);
    }

    private void execute(final @Nullable Long amount, final @Nullable TimeHandlerUnit unit, long value, final @Nullable TaskDelayType type, @Nullable Long originalDelay) {
        long delay = unit != null ? unit.getMultiplier() * Math.max(0, value) : 0;
        final long finalDelay = delay;
        if (originalDelay == null)
            originalDelay = finalDelay;
        final Long finalOriginalDelay = originalDelay;
        final boolean first;

        if (repeating == null) {
            this.repeating = amount != null;
            first = true;
        } else first = false;

        totalTaskCount++;
        if (thread == null) {
            if (async) {
                thread = new Thread(() -> execute(amount, unit, finalDelay, type, finalOriginalDelay), "Async | Task-Thread | Id: " + totalTaskCount);
                thread.start();
                return;
            }
            thread = Thread.currentThread();
        }

        if (amount != null)
            if (amount <= 0) {
                if (isAsync())
                    thread.interrupt();
                return;
            }

        while (delay > 0) {
            if (type != null) {
                if (first && type == TaskDelayType.ONLY_BETWEEN)
                    break;
                if (!first && type == TaskDelayType.ONLY_BEGINNING)
                    break;
            }

            final long remove = Math.min(delay, default_delay_ms);
            delay -= remove;
        }

        consumer.accept(this);

        this.execute(amount != null ? amount - 1 : null, unit, finalDelay, type, finalOriginalDelay);
    }

    public boolean isAsync() {
        return thread == Thread.currentThread();
    }

    public boolean isSync() {
        return !isAsync();
    }

    public @Deprecated Thread getThread() {
        return thread;
    }

    public boolean isThread(final @Nullable Thread thread) {
        if (thread == null)
            return false;
        return thread.equals(this.thread);
    }

    public boolean isRepeating() {
        if (repeating == null)
            return false;
        return repeating;
    }
}
