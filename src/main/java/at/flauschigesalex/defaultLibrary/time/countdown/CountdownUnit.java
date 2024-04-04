package at.flauschigesalex.defaultLibrary.time.countdown;

import at.flauschigesalex.defaultLibrary.time.TimeHandlerUnit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
@Getter
public enum CountdownUnit {
    SECOND(TimeHandlerUnit.SECOND),
    MILLI(TimeHandlerUnit.MILLI),
    ;

    private final TimeHandlerUnit timeHandlerUnit;
    CountdownUnit(final @NotNull TimeHandlerUnit unitAction) {
        this.timeHandlerUnit = unitAction;
    }
}
