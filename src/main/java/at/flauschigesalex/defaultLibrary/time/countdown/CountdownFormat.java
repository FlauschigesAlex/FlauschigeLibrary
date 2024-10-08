package at.flauschigesalex.defaultLibrary.time.countdown;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static at.flauschigesalex.defaultLibrary.time.countdown.CountdownDisplayType.*;
import static java.util.concurrent.TimeUnit.*;

@SuppressWarnings("unused")
@Getter
public class CountdownFormat {

    public static CountdownFormat smart() {
        return new CountdownFormat(HIDE_ANY_EMPTY_FIELD_SMART, new TimeUnit[]{
                DAYS, HOURS, MINUTES, SECONDS
        });
    }

    @Deprecated
    public static CountdownFormat old() {
        return new CountdownFormat(DISPLAY_ANY_FIELD, new TimeUnit[]{
                DAYS, HOURS, MINUTES, SECONDS
        });
    }

    public static CountdownFormat custom(final @NotNull Set<TimeUnit> displayFields) {
        return custom(displayFields, false);
    }

    public static CountdownFormat custom(final @NotNull Set<TimeUnit> displayFields, final boolean displayEmpty) {
        final HashSet<TimeUnit> displayFieldsModifiable = new HashSet<>(displayFields);
        for (final TimeUnit timeUnit : Set.of(NANOSECONDS, MICROSECONDS)) {
            if (!displayFieldsModifiable.contains(timeUnit))
                continue;

            displayFieldsModifiable.remove(timeUnit);
            System.err.println("["+CountdownFormat.class.getSimpleName()+"] Removed "+TimeUnit.class.getSimpleName()+" "+timeUnit+" since it's to small to be displayed.");
        }

        if (displayFieldsModifiable.isEmpty())
            throw new IllegalArgumentException("Displayed fields cannot be empty.");

        return new CountdownFormat(displayEmpty ? CUSTOM_DISPLAY_EMPTY_FIELD : CUSTOM_HIDE_EMPTY_FIELD, displayFieldsModifiable.toArray(TimeUnit[]::new));
    }

    private final CountdownDisplayType displayType;
    private final ArrayList<TimeUnit> visibleTimeUnits = new ArrayList<>();
    private HashMap<TimeUnit, String> representation = new HashMap<>(Map.of(
            DAYS, "d",
            HOURS, "h",
            MINUTES, "m",
            SECONDS, "s",
            MILLISECONDS, "ms"
    ));

    private CountdownFormat(final @NotNull CountdownDisplayType displayType, final @NotNull TimeUnit[] visibleTimeUnits) {
        this.displayType = displayType;
        this.visibleTimeUnits.addAll(List.of(visibleTimeUnits));
    }

    public void represent(final @NotNull Map<TimeUnit, String> representation) throws IllegalArgumentException {
        final Set<TimeUnit> missing = new HashSet<>();
        for (final TimeUnit unit : this.visibleTimeUnits)
            if (!representation.containsKey(unit))
                missing.add(unit);

        if (!missing.isEmpty()) {
            final StringBuilder exception = new StringBuilder("Failed to set new representation since the following fields are missing:");
            missing.forEach(timeUnit -> exception.append("\n - ").append(timeUnit));
            throw new IllegalArgumentException(exception.toString());
        }

        this.representation = new HashMap<>(representation);
    }

    String format(final long difference) {

        if (difference == 0)
            return "0 ms";

        long days = MILLISECONDS.toDays(difference);
        long hours = MILLISECONDS.toHours(difference) - DAYS.toHours(days);
        long minutes = MILLISECONDS.toMinutes(difference) - DAYS.toMinutes(days) - HOURS.toMinutes(hours);
        long seconds = MILLISECONDS.toSeconds(difference) - DAYS.toSeconds(days) - HOURS.toSeconds(hours) - MINUTES.toSeconds(minutes);
        long millis = MILLISECONDS.toMillis(difference) - DAYS.toMillis(days) - HOURS.toMillis(hours) - MINUTES.toMillis(minutes) - SECONDS.toMillis(seconds);

        if (displayType == DISPLAY_ANY_FIELD)
            return days + representation.get(DAYS) + " " + hours + representation.get(HOURS) + " " + minutes + representation.get(MINUTES) + " " + seconds + representation.get(SECONDS) + " " + millis + representation.get(MILLISECONDS);

        else if (displayType == HIDE_ANY_EMPTY_FIELD_SMART) {
            final ArrayList<String> back = new ArrayList<>();
            if (visibleTimeUnits.contains(DAYS) && days != 0)
                back.add(days + representation.get(DAYS));
            if (visibleTimeUnits.contains(HOURS) && (days != 0 || hours != 0))
                back.add(hours + representation.get(HOURS));
            if (visibleTimeUnits.contains(MINUTES) && (days != 0 || hours != 0 || minutes != 0))
                back.add(minutes + representation.get(MINUTES));
            if (visibleTimeUnits.contains(SECONDS) && (days != 0 || hours != 0 || minutes != 0 || seconds != 0))
                back.add(seconds + representation.get(SECONDS));
            if (visibleTimeUnits.contains(MILLISECONDS) && (days != 0 || hours != 0 || minutes != 0 || seconds != 0 || millis != 0))
                back.add(millis + representation.get(MILLISECONDS));

            final StringBuilder builder = new StringBuilder();
            for (String part : back) {
                if (!builder.isEmpty())
                    builder.append(" ");
                builder.append(part);
            }
            return builder.toString();
        } else if (displayType == HIDE_ANY_EMPTY_FIELD) {
            final ArrayList<String> empty = new ArrayList<>();
            if (visibleTimeUnits.contains(DAYS) && days != 0)
                empty.add(days + representation.get(DAYS));
            if (visibleTimeUnits.contains(HOURS) && hours != 0)
                empty.add(hours + representation.get(HOURS));
            if (visibleTimeUnits.contains(MINUTES) && minutes != 0)
                empty.add(minutes + representation.get(MINUTES));
            if (visibleTimeUnits.contains(SECONDS) && seconds != 0)
                empty.add(seconds + representation.get(SECONDS));
            if (visibleTimeUnits.contains(MILLISECONDS) && millis != 0)
                empty.add(millis + representation.get(MILLISECONDS));

            final StringBuilder builder = new StringBuilder();
            for (String part : empty) {
                if (!builder.isEmpty())
                    builder.append(" ");
                builder.append(part);
            }
            return builder.toString();
        } else if (displayType.toString().startsWith("CUSTOM_")) {
            final ArrayList<String> custom = new ArrayList<>();
            if (!visibleTimeUnits.contains(DAYS))
                hours += DAYS.toHours(days);
            if (!visibleTimeUnits.contains(HOURS))
                minutes += HOURS.toMinutes(hours);
            if (!visibleTimeUnits.contains(MINUTES))
                seconds += MINUTES.toSeconds(minutes);
            if (!visibleTimeUnits.contains(SECONDS))
                millis += SECONDS.toMillis(seconds);

            final boolean displayEmpty = displayType == CUSTOM_DISPLAY_EMPTY_FIELD;

            if (displayEmpty) {
                if (visibleTimeUnits.contains(DAYS))
                    custom.add(days + representation.get(DAYS));
                if (visibleTimeUnits.contains(HOURS))
                    custom.add(hours + representation.get(HOURS));
                if (visibleTimeUnits.contains(MINUTES))
                    custom.add(minutes + representation.get(MINUTES));
                if (visibleTimeUnits.contains(SECONDS))
                    custom.add(seconds + representation.get(SECONDS));
                if (visibleTimeUnits.contains(MILLISECONDS))
                    custom.add(millis + representation.get(MILLISECONDS));
            } else {
                if (visibleTimeUnits.contains(DAYS) && days != 0)
                    custom.add(days + representation.get(DAYS));
                if (visibleTimeUnits.contains(HOURS) && hours != 0)
                    custom.add(hours + representation.get(HOURS));
                if (visibleTimeUnits.contains(MINUTES) && minutes != 0)
                    custom.add(minutes + representation.get(MINUTES));
                if (visibleTimeUnits.contains(SECONDS) && seconds != 0)
                    custom.add(seconds + representation.get(SECONDS));
                if (visibleTimeUnits.contains(MILLISECONDS) && millis != 0)
                    custom.add(millis + representation.get(MILLISECONDS));
            }

            final StringBuilder builder = new StringBuilder();
            for (String part : custom) {
                if (!builder.isEmpty())
                    builder.append(" ");
                builder.append(part);
            }
            return builder.toString();
        }

        return "null";
    }
}
