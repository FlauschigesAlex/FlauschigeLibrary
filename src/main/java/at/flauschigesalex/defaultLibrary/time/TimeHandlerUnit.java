package at.flauschigesalex.defaultLibrary.time;

import lombok.Getter;

@Getter
public enum TimeHandlerUnit {

    MILLI(1, (milli, length, multiplier) -> Long.sum(milli, length)),
    SECOND(1000, (milli, length, multiplier) -> MILLI.perform(milli, length * multiplier)),
    MINUTE(60, (milli, length, multiplier) -> SECOND.perform(milli, length * multiplier)),
    HOUR(60, (milli, length, multiplier) -> MINUTE.perform(milli, length * multiplier)),
    DAY(24, (milli, length, multiplier) -> HOUR.perform(milli, length * multiplier)),
    @Deprecated WEEK(7, (milli, length, multiplier) -> DAY.perform(milli, length * multiplier)),
    /**
     * @apiNote Due to incompatibility with leap years this unit will always operate with <b>30 days</b>.
     */
    MONTH(30, (milli, length, multiplier) -> DAY.perform(milli, length * multiplier)),
    /**
     * @apiNote Due to incompatibility with leap years this unit will always operate with <b>365 days</b>.
     */
    YEAR(365, (milli, length, multiplier) -> DAY.perform(milli, length * multiplier)),
    ;

    private final long multiplier;
    private final TimeHandlerUnitAction unitAction;

    TimeHandlerUnit(final long multiplier, TimeHandlerUnitAction unitAction) {
        this.multiplier = multiplier;
        this.unitAction = unitAction;
    }

    public long perform(final long epochMilli, final long length) {
        return unitAction.runAction(epochMilli, length, multiplier);
    }
}

