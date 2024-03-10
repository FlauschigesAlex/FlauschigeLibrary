package at.flauschigesalex.defaultLibrary.execution.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

import static java.lang.Thread.sleep;

@SuppressWarnings({"UnusedReturnValue", "unused", "SameParameterValue", "unchecked"})
@Getter
public sealed class Task permits AsyncTask {

    @Setter
    private static RepeatDelayType defaultDelayType = RepeatDelayType.ALWAYS;

    @CheckReturnValue
    public static Task createTask(final @NotNull Consumer<Task> consumer) {
        return new Task(consumer);
    }
    @CheckReturnValue
    public static AsyncTask createAsyncTask(final @NotNull Consumer<Task> consumer) {
        return new AsyncTask(consumer);
    }

    protected final Consumer<Task> consumer;
    Task(final Consumer<Task> consumer) {
        this.consumer = consumer;
    }

    private boolean running = true;
    public void start() {
        if (!running)
            return;
        consumer.accept(this);
    }
    public final <T extends Task> T restart(final boolean instant) {
        running = true;
        if (instant)
            this.start();
        return (T) this;
    }
    public final void stop() {
        running = false;
    }

    @SneakyThrows
    public void startDelayed(final @Range(from = 1, to = Long.MAX_VALUE) long delay) {
        sleep(delay);
        start();
    }

    /**
     * @deprecated May cause {@link StackOverflowError}.
     */
    public final void repeat() {
        repeat(Integer.MAX_VALUE);
    }
    public void repeat(final @Range(from = 1, to = Integer.MAX_VALUE) int times) {
        for (int time = 0; time < times; time++) {
            start();
        }
    }

    public final void repeatDelayed(final @Range(from = 1, to = Long.MAX_VALUE) long delay) {
        repeatDelayed(Integer.MAX_VALUE, delay);
    }

    public void repeatDelayed(final @Range(from = 1, to = Integer.MAX_VALUE) int times, final @Range(from = 1, to = Long.MAX_VALUE) long delay) {
        repeatDelayed(times, delay, defaultDelayType);
    }

    /**
     * @deprecated May cause {@link StackOverflowError}.
     */
    public final void repeatDelayed(final @Range(from = 1, to = Long.MAX_VALUE) long delay, final @NotNull RepeatDelayType delayType) {
        repeatDelayed(Integer.MAX_VALUE, delay, delayType);
    }

    public void repeatDelayed(@Range(from = 1, to = Integer.MAX_VALUE) int times, final @Range(from = 1, to = Long.MAX_VALUE) long delay, final @NotNull RepeatDelayType delayType) {
        if (delayType.hasInitialDelay())
            startDelayed(delay);
        else
            start();
        times--;

        for (int time = 0; time < times; time++) {
            if (delayType.hasBetweenDelay())
                startDelayed(delay);
            else
                start();
        }
    }

    public enum RepeatDelayType {
        ONLY_INITIAL(true, false),
        ONLY_BETWEEN(false, true),
        ALWAYS(true, true),
        ;

        private final boolean initialDelay;
        private final boolean betweenDelay;

        RepeatDelayType(boolean initialDelay, boolean betweenDelay) {
            this.initialDelay = initialDelay;
            this.betweenDelay = betweenDelay;
        }

        public boolean hasInitialDelay() {
            return initialDelay;
        }
        public boolean hasBetweenDelay() {
            return betweenDelay;
        }
    }
}
