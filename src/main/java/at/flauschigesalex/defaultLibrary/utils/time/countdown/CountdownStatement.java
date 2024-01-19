package at.flauschigesalex.defaultLibrary.utils.time.countdown;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@SuppressWarnings("unused")
public final class CountdownStatement {

    private final long trueDifference;

    CountdownStatement(final long trueDifference) {
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
        return getTrueDifference() < 0;
    }
}
