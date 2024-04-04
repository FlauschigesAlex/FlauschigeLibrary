package at.flauschigesalex.defaultLibrary.time.countdown;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@SuppressWarnings("unused")
public final class Countdown {

    private final long trueDifference;

    Countdown(final long trueDifference) {
        this.trueDifference = trueDifference;
    }

    public String getCountdown() {
        return getCountdown(CountdownFormat.smart());
    }

    public String getCountdown(final @NotNull CountdownFormat format) {
        return format.format(getTrueDifference());
    }

    public long getDisplayDifference() {
        return Math.abs(getTrueDifference());
    }

    public boolean hasExpired() {
        return getTrueDifference() <= 0;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Countdown statement)
            return statement.trueDifference == this.trueDifference;
        return false;
    }

    public String toString() {
        return this.getCountdown();
    }
}
