package at.flauschigesalex.defaultLibrary.time.countdown;

import at.flauschigesalex.defaultLibrary.time.TimeHandler;
import lombok.Getter;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

@Getter
@SuppressWarnings("unused")
public final class CountdownHandler {

    private final TimeHandler start;

    private CountdownHandler(final @NotNull TimeHandler start) {
        this.start = start;
    }

    public static @CheckReturnValue Countdown until(final @NotNull TimeHandler end) {
        return handle(TimeHandler.now(), end);
    }

    public static @CheckReturnValue Countdown handle(final @NotNull TimeHandler start, final @NotNull TimeHandler end) {
        return new CountdownHandler(start).createCountdown(end);
    }

    @CheckReturnValue
    public Countdown createCountdown(final @NotNull TimeHandler end) {
        final long difference = end.getEpochMilli() - getStart().getEpochMilli();
        return new Countdown(difference);
    }
}