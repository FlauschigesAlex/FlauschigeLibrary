package at.flauschigesalex.defaultLibrary.utils.time;

import at.flauschigesalex.defaultLibrary.utils.Printable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@SuppressWarnings("unused")
public final class TimeHandler extends Printable implements Cloneable {

    public static boolean isEpochMillisecond(final long value) {
        return value > 100000000000L;
    }

    public static boolean isEpochSecond(final long value) {
        return !isEpochMillisecond(value);
    }

    /**
     * @param value Timestamp in S or MS
     */
    public static TimeHandler handle(final long value) {
        return new TimeHandler(isEpochMillisecond(value) ? value : value * 1000);
    }

    public static TimeHandler handle(final @NotNull Instant instant) {
        return handle(instant.toEpochMilli());
    }

    public static TimeHandler handle(final @NotNull ZonedDateTime zonedDateTime) {
        return handle(zonedDateTime.toInstant());
    }

    /**
     * @deprecated
     * @see TimeHandler#clone() Cloneable
     */
    public static TimeHandler clone(final @NotNull TimeHandler handler) {
        return handle(handler.getInstant());
    }

    public static TimeHandler now() {
        return handle(Instant.now());
    }

    private final long epochMillisecond;

    TimeHandler(final long value) {
        this.epochMillisecond = isEpochMillisecond(value) ? value : value * 1000;
    }

    public long getEpochMilli() {
        return epochMillisecond;
    }
    public long getEpochSecond() {
        return getEpochMilli()*1000;
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

    public TimeHandler add(final @NotNull TimeHandlerUnit timeUnit, @Range(from = 1, to = Long.MAX_VALUE) final long length) {
        return modify(timeUnit, length);
    }

    public TimeHandler subtract(final @NotNull TimeHandlerUnit timeUnit, @Range(from = 1, to = Long.MAX_VALUE) final long length) {
        return modify(timeUnit, -length);
    }

    TimeHandler modify(final @NotNull TimeHandlerUnit timeUnit, final long length) {
        return handle(timeUnit.perform(getEpochMilli(), length));
    }

    @Override
    protected TimeHandler clone() throws CloneNotSupportedException {
        return (TimeHandler) super.clone();
    }
}
