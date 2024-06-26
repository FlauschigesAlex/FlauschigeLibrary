package at.flauschigesalex.defaultLibrary.project.task;

import at.flauschigesalex.defaultLibrary.time.TimeHandlerUnit;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
    private Executor asyncExecutor;

    private Boolean repeating;

    private Task(final @NotNull Consumer<Task> consumer, final boolean async) {
        this.consumer = consumer;
        this.async = async;
    }

    public void execute() {
        this.run(1L, null, 0, null, false);
    }

    public void executeDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.run(1L, unit, value, null, false);
    }

    public void repeat() {
        this.run(null, null, 0, null, true);
    }

    public void repeat(final long amount) {
        this.run(amount, null, 0, null, true);
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value) {
        this.run(null, unit, value, TaskDelayType.defaultType(), true);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value) {
        this.run(amount, unit, value, TaskDelayType.defaultType(), true);
    }

    public void repeatDelayed(final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(null, unit, value, type, true);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeHandlerUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(amount, unit, value, type, true);
    }

    @SneakyThrows
    private void run(final @Nullable Long amount, final @Nullable TimeHandlerUnit unit, long value, final @Nullable TaskDelayType type, final boolean first) {
        final long[] ms_values = new long[]{unit != null ? unit.perform(0, value) : -1};
        if (amount != null && amount <= 0)
            return;

        if (async && asyncExecutor == null) {
            asyncExecutor = Executors.newCachedThreadPool();
            asyncExecutor.execute(() -> {
                this.run(amount, unit, value, type, first);
            });
            return;
        }

        if (unit != null)
            while (ms_values[0] > 0) {
                if (type == TaskDelayType.ONLY_BETWEEN && first)
                    break;
                if (type == TaskDelayType.ONLY_BEGINNING && !first)
                    break;

                final long ms_remove = Math.min(ms_values[0], default_delay_ms);
                ms_values[0] -= ms_remove;
                sleep(ms_remove);
            }

        consumer.accept(this);

        this.run(amount != null ? amount-1 : null, unit, value, type, false);
    }

    public boolean isSync() {
        return !isAsync();
    }

    public boolean isRepeating() {
        if (repeating == null)
            return false;
        return repeating;
    }
}
