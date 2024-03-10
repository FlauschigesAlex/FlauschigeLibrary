package at.flauschigesalex.defaultLibrary.execution.tasks;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

@Getter(AccessLevel.PRIVATE)
public final class AsyncTask extends Task {

    private boolean async;
    AsyncTask(final Consumer<Task> consumer) {
        super(consumer);
    }

    @Override
    public void start() {
        if (isAsync()) {
            super.start();
            return;
        }
        async = true;
        new Thread(this::start).start();
    }

    @Override
    public void startDelayed(@Range(from = 1, to = Long.MAX_VALUE) long delay) {
        if (isAsync()) {
            super.startDelayed(delay);
            return;
        }
        async = true;
        new Thread(() -> this.startDelayed(delay)).start();
    }

    @Override
    public void repeat(@Range(from = 1, to = Integer.MAX_VALUE) int times) {
        if (isAsync()) {
            super.repeat(times);
            return;
        }
        async = true;
        new Thread(() -> this.repeat(times)).start();
    }

    @Override
    public void repeatDelayed(@Range(from = 1, to = Integer.MAX_VALUE) int times, @Range(from = 1, to = Long.MAX_VALUE) long delay) {
        if (isAsync()) {
            super.repeatDelayed(times, delay);
            return;
        }
        async = true;
        new Thread(() -> this.repeatDelayed(times, delay)).start();
    }

    @Override
    public void repeatDelayed(@Range(from = 1, to = Integer.MAX_VALUE) int times, @Range(from = 1, to = Long.MAX_VALUE) long delay, @NotNull RepeatDelayType delayType) {
        if (isAsync()) {
            super.repeatDelayed(times, delay, delayType);
            return;
        }
        async = true;
        new Thread(() -> this.repeatDelayed(times, delay, delayType)).start();
    }
}
