package at.flauschigesalex.defaultLibrary.execution.tasks;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Getter(AccessLevel.PRIVATE)
public final class AsyncTask extends Task {

    private boolean async;
    AsyncTask(final @NotNull TaskAction taskAction) {
        super(taskAction);
    }

    @Override
    public void execute() {
        if (isAsync()) {
            super.execute();
            return;
        }
        async = true;
        new Thread(this::execute).start();
    }

    @Override
    public void executeDelayed(@Range(from = 1, to = Long.MAX_VALUE) long delay) {
        if (isAsync()) {
            super.executeDelayed(delay);
            return;
        }
        async = true;
        new Thread(() -> this.executeDelayed(delay)).start();
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
