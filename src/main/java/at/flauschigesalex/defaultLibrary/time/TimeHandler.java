package at.flauschigesalex.defaultLibrary.time;

import at.flauschigesalex.defaultLibrary.databases.mongo.LibraryMongoInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class TimeHandler implements Cloneable, LibraryMongoInformation {

    private final long epochMillisecond;

    TimeHandler(final long value) {
        this.epochMillisecond = isEpochMillisecond(value) ? value : value * 1000;
    }

    public static boolean isEpochMillisecond(final @NotNull Long value) {
        return value > 100000000000L;
    }

    public static boolean isEpochSecond(final long value) {
        return !isEpochMillisecond(value);
    }

    /**
     * @param value Timestamp in S or MS
     */
    public static TimeHandler handle(final @NotNull Long value) {
        return new TimeHandler(isEpochMillisecond(value) ? value : value * 1000);
    }

    public static TimeHandler handle(final @NotNull Instant instant) {
        return handle(instant.toEpochMilli());
    }

    public static TimeHandler handle(final @NotNull ZonedDateTime zonedDateTime) {
        return handle(zonedDateTime.toInstant());
    }

    /**
     * @see TimeHandler#clone() Cloneable
     * @deprecated
     */
    public static TimeHandler clone(final @NotNull TimeHandler handler) {
        return handle(handler.getInstant());
    }

    public static TimeHandler now() {
        return handle(Instant.now());
    }

    public long getEpochMilli() {
        return epochMillisecond;
    }

    public long getEpochSecond() {
        return getEpochMilli() / 1000;
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(getEpochMilli());
    }

    public ZonedDateTime getZonedDateTime(@Nullable ZoneId zoneId) {
        if (zoneId == null)
            zoneId = getDefaultZoneId();
        return ZonedDateTime.ofInstant(getInstant(), zoneId);
    }

    public LocalDateTime getLocalDateTime(@Nullable ZoneId zoneId) {
        if (zoneId == null)
            zoneId = getDefaultZoneId();
        return LocalDateTime.ofInstant(getInstant(), zoneId);
    }

    public ZoneId getDefaultZoneId() {
        return ZoneId.systemDefault();
    }

    public @Deprecated(forRemoval = true) TimeHandler add(final @NotNull TimeUnit timeUnit, @Range(from = 1, to = Long.MAX_VALUE) final long length) {
        return modify(timeUnit, length);
    }

    public TimeHandler plus(final @NotNull TimeUnit timeUnit, @Range(from = 1, to = Long.MAX_VALUE) final long length) {
        return modify(timeUnit, length);
    }

    public TimeHandler subtract(final @NotNull TimeUnit timeUnit, @Range(from = 1, to = Long.MAX_VALUE) final long length) {
        return modify(timeUnit, -length);
    }

    TimeHandler modify(final @NotNull TimeUnit timeUnit, final long value) {
        return handle(timeUnit.toMillis(value));
    }

    public String toString() {
        return String.valueOf(epochMillisecond);
    }

    protected TimeHandler clone() throws CloneNotSupportedException {
        return (TimeHandler) super.clone();
    }
}
