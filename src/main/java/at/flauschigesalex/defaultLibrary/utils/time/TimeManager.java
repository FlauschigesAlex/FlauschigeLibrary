package at.flauschigesalex.defaultLibrary.utils.time;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@SuppressWarnings("unused")
public final class TimeManager extends TimeManagerLimited {

    /**
     * @param value Timestamp in S or MS
     */
    public static TimeManager create(long value) {
        return new TimeManager(isEpochMillisecond(value) ? value : value * 1000);
    }

    public static TimeManager create(@NotNull Instant instant) {
        return new TimeManager(instant.toEpochMilli());
    }

    public static TimeManager create(@NotNull ZonedDateTime zonedDateTime) {
        return new TimeManager(zonedDateTime.toInstant().toEpochMilli());
    }

    public static TimeManager now() {
        return new TimeManager(Instant.now().toEpochMilli());
    }

    final TimeFormatter timeFormatter = new TimeFormatter(this);

    TimeManager(long value) {
        super(value);
    }

    /**
     * @param value Timestamp in S or MS
     * @return difference between Timestamps in MS
     */
    public long until(long value) {
        return (isEpochMillisecond(value) ? value : value * 1000) - getEpochMillisecond();
    }

    public long until(@NotNull Instant instant) {
        return instant.toEpochMilli() - getEpochMillisecond();
    }

    public long until(@NotNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().getEpochSecond() - getEpochSecond();
    }

    public long until(@NotNull TimeManager timeManager) {
        return timeManager.getEpochSecond() - getEpochSecond();
    }

    /**
     * @param value Timestamp in Second
     * @return difference between Timestamps
     * @deprecated use {@link #timeUntil(ZonedDateTime)} instead
     */
    public TimeManager timeSecondUntil(long value) {
        return timeMillisecondUntil(value * 1000);
    }

    /**
     * @param value Timestamp in Millisecond
     * @return difference between Timestamps
     * @deprecated use {@link #timeUntil(ZonedDateTime)} instead
     */
    public TimeManager timeMillisecondUntil(long value) {
        return new TimeManager(untilMillisecond(value));
    }

    public TimeManager timeUntil(@NotNull Instant instant) {
        return TimeManager.create(this.until(instant));
    }

    public TimeManager timeUntil(@NotNull ZonedDateTime zonedDateTime) {
        return TimeManager.create(this.until(zonedDateTime));
    }

    public TimeManager timeUntil(@NotNull TimeManager timeManager) {
        return TimeManager.create(this.until(timeManager));
    }

    public LocalDateTime getLocalDateTime(@NotNull ZoneId zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getEpochMillisecond()), zoneId);
    }

    public ZonedDateTime getZonedDateTime(@NotNull ZoneId zoneId) {
        return ZonedDateTime.of(getLocalDateTime(), zoneId);
    }

    public TimeFormatter getTimeFormatter(@NotNull String pattern) {
        return timeFormatter.setPattern(pattern);
    }

    public TimeFormatter copyTimeFormatter(@NotNull TimeManager timeManager) {
        return timeFormatter.setPattern(timeManager.timeFormatter.getPattern());
    }

    private long untilMillisecond(long value) {
        return value - getEpochMillisecond();
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getEpochMillisecond()), ZoneId.systemDefault());
    }

    public ZonedDateTime getZonedDateTime() {
        ZonedDateTime t = null;
        return ZonedDateTime.of(getLocalDateTime(), ZoneId.systemDefault());
    }

    public TimeFormatter getTimeFormatter() {
        return timeFormatter.setPattern(TimeFormatter.defaultPattern);
    }
}
