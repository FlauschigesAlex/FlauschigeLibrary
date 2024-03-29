package at.flauschigesalex.defaultLibrary.time.countdown;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static at.flauschigesalex.defaultLibrary.time.countdown.CountdownDisplayType.*;

@Getter
public class CountdownFormat {

    //TODO CUSTOM

    private final CountdownDisplayType displayType;
    private final ArrayList<TimeUnit> visibleTimeUnits = new ArrayList<>();

    private CountdownFormat(final @NotNull CountdownDisplayType displayType, final @NotNull TimeUnit[] visibleTimeUnits) {
        this.displayType = displayType;
        this.visibleTimeUnits.addAll(List.of(visibleTimeUnits));
    }

    public static CountdownFormat smart() {
        return new CountdownFormat(HIDE_ANY_EMPTY_FIELD_SMART, new TimeUnit[]{
                TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS
        });
    }

    @Deprecated
    public static CountdownFormat old() {
        return new CountdownFormat(DISPLAY_ANY_FIELD, new TimeUnit[]{
                TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS
        });
    }

    String format(final long difference) {

        if (difference == 0)
            return "0 ms";

        long days = TimeUnit.MILLISECONDS.toDays(difference);
        long hours = TimeUnit.MILLISECONDS.toHours(difference) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(difference) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(difference) - TimeUnit.DAYS.toSeconds(days) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);
        long millis = TimeUnit.MILLISECONDS.toMillis(difference) - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);

        if (displayType == DISPLAY_ANY_FIELD)
            return days + "d " + hours + "h " + minutes + "m " + seconds + "s " + millis + "ms";

        final ArrayList<String> back = new ArrayList<>();
        if (visibleTimeUnits.contains(TimeUnit.DAYS) && days != 0)
            back.add(days + "d");
        if (visibleTimeUnits.contains(TimeUnit.HOURS) && (days != 0 || hours != 0))
            back.add(hours + "h");
        if (visibleTimeUnits.contains(TimeUnit.MINUTES) && (days != 0 || hours != 0 || minutes != 0))
            back.add(minutes + "m");
        if (visibleTimeUnits.contains(TimeUnit.SECONDS) && (days != 0 || hours != 0 || minutes != 0 || seconds != 0))
            back.add(seconds + "s");
        if (visibleTimeUnits.contains(TimeUnit.MILLISECONDS) && (days != 0 || hours != 0 || minutes != 0 || seconds != 0 || millis != 0))
            back.add(millis + "ms");

        if (displayType == HIDE_ANY_EMPTY_FIELD_SMART) {
            final StringBuilder builder = new StringBuilder();
            for (String part : back) {
                if (!builder.isEmpty())
                    builder.append(" ");
                builder.append(part);
            }
            return builder.toString();
        }

        final ArrayList<String> empty = new ArrayList<>();
        if (visibleTimeUnits.contains(TimeUnit.DAYS) && days != 0)
            empty.add(days + "d");
        if (visibleTimeUnits.contains(TimeUnit.HOURS) && hours != 0)
            empty.add(hours + "h");
        if (visibleTimeUnits.contains(TimeUnit.MINUTES) && minutes != 0)
            empty.add(minutes + "m");
        if (visibleTimeUnits.contains(TimeUnit.SECONDS) && seconds != 0)
            empty.add(seconds + "s");
        if (visibleTimeUnits.contains(TimeUnit.MILLISECONDS) && millis != 0)
            empty.add(millis + "ms");

        if (displayType == HIDE_ANY_EMPTY_FIELD) {
            final StringBuilder builder = new StringBuilder();
            for (String part : empty) {
                if (!builder.isEmpty())
                    builder.append(" ");
                builder.append(part);
            }
            return builder.toString();
        }

        return "";
    }
}
