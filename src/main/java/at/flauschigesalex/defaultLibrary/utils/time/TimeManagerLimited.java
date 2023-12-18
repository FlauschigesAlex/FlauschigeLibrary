package at.flauschigesalex.defaultLibrary.utils.time;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@SuppressWarnings("unused")
public class TimeManagerLimited {


    public static boolean isEpochMillisecond(long value) {
        return value > 100000000000L;
    }

    public static boolean isEpochSecond(long value) {
        return !isEpochMillisecond(value);
    }

    /**
     * @param value Timestamp in S or MS
     */
    public static TimeManagerLimited create(long value) {
        return new TimeManagerLimited(isEpochMillisecond(value) ? value : value * 1000);
    }

    public static TimeManagerLimited create(@NotNull Instant instant) {
        return new TimeManagerLimited(instant.toEpochMilli());
    }

    public static TimeManagerLimited create(@NotNull LocalDateTime localDateTime) {
        return new TimeManagerLimited(ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static TimeManagerLimited create(@NotNull ZonedDateTime zonedDateTime) {
        return new TimeManagerLimited(zonedDateTime.toInstant().toEpochMilli());
    }

    public static TimeManagerLimited now() {
        return new TimeManagerLimited(Instant.now().toEpochMilli());
    }
    final Countdown countdown = new Countdown(this);
    private final long epochMillisecond;

    TimeManagerLimited(long value) {
        this.epochMillisecond = value;
    }

    public long getEpochSecond() {
        return getEpochMillisecond() / 1000;
    }
}
