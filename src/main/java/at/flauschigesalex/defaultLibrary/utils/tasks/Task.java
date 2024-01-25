package at.flauschigesalex.defaultLibrary.utils.tasks;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import static java.lang.Thread.sleep;

@SuppressWarnings({"UnusedReturnValue", "unused", "SameParameterValue"})
@Getter
public class Task {

    @Setter
    private static RepeatDelayType defaultDelayType = RepeatDelayType.ALWAYS;

    @CheckReturnValue
    public static Task createTask(final @NotNull TaskAction taskAction) {
        return new Task(taskAction);
    }
    @CheckReturnValue
    public static AsyncTask createAsyncTask(final @NotNull TaskAction taskAction) {
        return new AsyncTask(taskAction);
    }

    protected final @NotNull TaskAction taskAction;
    Task(final @NotNull TaskAction taskAction) {
        this.taskAction = taskAction;
    }

    public void execute() {
        taskAction.runTask();
    }

    @SneakyThrows
    public void executeDelayed(final @Range(from = 1, to = Long.MAX_VALUE) long delay) {
        sleep(delay);
        execute();
    }

    /**
     * @deprecated May cause {@link StackOverflowError}.
     */
    public final void repeat() {
        repeat(Integer.MAX_VALUE);
    }
    public void repeat(final @Range(from = 1, to = Integer.MAX_VALUE) int times) {
        for (int time = 0; time < times; time++) {
            execute();
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
            executeDelayed(delay);
        else
            execute();
        times--;

        for (int time = 0; time < times; time++) {
            if (delayType.hasBetweenDelay())
                executeDelayed(delay);
            else
                execute();
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
