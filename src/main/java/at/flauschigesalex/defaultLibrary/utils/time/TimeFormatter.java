package at.flauschigesalex.defaultLibrary.utils.time;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class TimeFormatter {

    static final String defaultPattern = "dd.MM.yyyy kk:mm:ss";
    private final TimeManager timeManager;
    @Getter(AccessLevel.NONE)
    private String customPattern;

    TimeFormatter(TimeManager timeManager) {
        this.timeManager = timeManager;
    }

    public String formatted() {
        String formatted = getPattern();

        formatted = formatted
                .replace("yyyy", String.valueOf(getTimeManager().getZonedDateTime().getYear()))
                .replace("MM", format(getTimeManager().getZonedDateTime().getMonthValue()))
                .replace("dd", format(getTimeManager().getZonedDateTime().getDayOfMonth()))
                .replace("hh", format(getTimeManager().getZonedDateTime().getHour() > 12 ?
                        getTimeManager().getZonedDateTime().getHour() - 12 : getTimeManager().getZonedDateTime().getHour()))
                .replace("kk", format(getTimeManager().getZonedDateTime().getHour()))
                .replace("mm", format(getTimeManager().getZonedDateTime().getMinute()))
                .replace("ss", format(getTimeManager().getZonedDateTime().getSecond()));

        return formatted;
    }

    String format(int v) {
        if (v < 0)
            return "??";
        return v < 10 ? "0" + v : String.valueOf(v);
    }

    public String getPattern() {
        String formatted = null;
        if (customPattern != null)
            formatted = customPattern;
        if (formatted == null)
            formatted = defaultPattern;
        return formatted;
    }

    public TimeFormatter setPattern(String pattern) {
        this.customPattern = pattern;
        return this;
    }
}
