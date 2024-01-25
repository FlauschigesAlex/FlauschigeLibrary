package at.flauschigesalex.defaultLibrary.time.countdown;

import at.flauschigesalex.defaultLibrary.utils.AutoDisplayable;
import at.flauschigesalex.defaultLibrary.time.TimeHandler;
import lombok.Getter;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.time.ZonedDateTime;

@Getter
@SuppressWarnings("unused")
public final class CountdownHandler extends AutoDisplayable {

    private final TimeHandler start;

    private CountdownHandler(final @NotNull TimeHandler start) {
        this.start = start;
    }

    public static CountdownHandler now() {
        return handle(TimeHandler.now());
    }

    public static CountdownHandler handle(final long value) {
        return handle(TimeHandler.handle(value));
    }

    public static CountdownHandler handle(final @NotNull Instant instant) {
        return handle(instant.toEpochMilli());
    }

    public static CountdownHandler handle(final @NotNull ZonedDateTime zonedDateTime) {
        return handle(zonedDateTime.toInstant());
    }

    public static CountdownHandler handle(final @NotNull TimeHandler start) {
        return new CountdownHandler(start);
    }

    @CheckReturnValue
    public CountdownStatement createCountdown(final @NotNull ZonedDateTime end) {
        return createCountdown(TimeHandler.handle(end));
    }

    @CheckReturnValue
    public CountdownStatement createCountdown(final @NotNull Instant end) {
        return createCountdown(TimeHandler.handle(end));
    }

    @CheckReturnValue
    public CountdownStatement createCountdown(final long end) {
        return createCountdown(TimeHandler.handle(end));
    }

    @CheckReturnValue
    public CountdownStatement createCountdown(final @NotNull TimeHandler end) {
        final long difference = end.getEpochMilli() - getStart().getEpochMilli();
        return new CountdownStatement(difference);
    }
}
