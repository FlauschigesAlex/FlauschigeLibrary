package at.flauschigesalex.defaultLibrary.project.task;

import at.flauschigesalex.defaultLibrary.time.TimeHandlerUnit;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final @Getter boolean async;

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
        this.run(1L, null, 0, null);
    }

    public void executeDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.run(1L, unit, value, null);
    }

    public void repeat() {
        this.run(null, null, 0, null);
    }

    public void repeat(final long amount) {
        this.run(amount, null, 0, null);
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.run(null, unit, value, TaskDelayType.defaultType());
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value) {
        this.run(amount, unit, value, TaskDelayType.defaultType());
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(null, unit, value, type);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(amount, unit, value, type);
    }

    @SneakyThrows
    private void run(final @Nullable Long amount, final @Nullable TimeHandlerUnit unit, long value, final @Nullable TaskDelayType type) {
        final long[] ms_values = new long[]{unit != null ? unit.perform(0, value) : -1};
        if (amount != null && amount <= 0)
            return;

        if (thread == null) {
            totalTaskCount++;
            if (async) {
                thread = new Thread(() -> {
                    this.run(amount, unit, value, type);
                }, "Async-Task | Thread | id: "+totalTaskCount);
                thread.start();
                return;
            }
            thread = Thread.currentThread();
        }

        if (unit != null)
            while (ms_values[0] > 0) {
                //TODO STOP PREDICATE
                final long ms_remove = Math.min(ms_values[0], default_delay_ms);
                ms_values[0] -= ms_remove;
                sleep(ms_remove);
            }

        consumer.accept(this);

        this.run(amount != null ? amount-1 : null, unit, value, type);
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
