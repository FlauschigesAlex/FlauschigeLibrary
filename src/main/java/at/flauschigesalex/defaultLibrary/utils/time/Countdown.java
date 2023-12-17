package at.flauschigesalex.defaultLibrary.utils.time;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"DataFlowIssue", "unused", "UnusedReturnValue"})
public final class Countdown {

    CountdownFormatter format;
    private final TimeManagerLimited timeManager;
    private long[] values;

    Countdown(@NotNull TimeManagerLimited timeManager) {
        this.timeManager = timeManager;
        this.format = new CountdownFormatter(timeManager, this);
        this.format = this.normal();
    }

    public CountdownFormatter custom() {
        return format.clear();
    }

    public String format() {
        generateValue();
        String string = format.builder.toString();

        for (int value = 0; value < values.length; value++) {
            if (!format.getEnabledFields()[value])
                continue;
            string = string.replace("{" + format.byID(value).toString().toLowerCase() + "}", String.valueOf(values[value]));
        }

        return string;
    }

    public String smartFormat() {
        return smartFormat(CustomCountdownFormat.defaultDisplay());
    }

    public String smartFormat(@NotNull CustomCountdownFormat display) {
        generateValue();
        custom();

        final StringBuilder builder = new StringBuilder();
        boolean show = false;

        for (int value = 0; value < values.length; value++) {
            if (values[value] > 0) show = true;
            if (!show) continue;
            if (display.isHideZeroField() && values[value] <= 0) continue;
            format.field(value);
            if (display.getCustomFields()[value] == null) continue;
            format.append(display.getCustomFields()[value]);
        }

        return format();
    }

    public double inSecondsDouble() {
        return as(CustomCountdownFormat.CustomFormatField.SECOND);
    }

    public double inMinutesDouble() {
        return as(CustomCountdownFormat.CustomFormatField.MINUTE);
    }

    public double inHoursDouble() {
        return as(CustomCountdownFormat.CustomFormatField.HOUR);
    }

    public double inDaysDouble() {
        return as(CustomCountdownFormat.CustomFormatField.DAY);
    }

    /**
     * @return Time in seconds as long
     * @deprecated return value may be inaccurate
     */
    public long inSeconds() {
        return (long) inSecondsDouble();
    }

    /**
     * @return Time in seconds as long
     * @deprecated return value may be inaccurate
     */
    public long inMinutes() {
        return (long) inMinutesDouble();
    }

    /**
     * @return Time in seconds as long
     * @deprecated return value may be inaccurate
     */
    public long inHours() {
        return (long) inHoursDouble();
    }

    /**
     * @return Time in seconds as long
     * @deprecated return value may be inaccurate
     */
    public long inDays() {
        return (long) inDaysDouble();
    }

    CountdownFormatter normal() {
        return format.clear().dayField("d ").hourField("h ").minuteField("m ").secondField("s ");
    }

    long getValue(CustomCountdownFormat.CustomFormatField formatField) {
        return values[formatField.getPosition()];
    }

    double as(CustomCountdownFormat.CustomFormatField formatField) {
        generateValue();
        double[] tempArrayAdd = Arrays.stream(values).asDoubleStream().toArray();
        double[] tempArraySub = Arrays.stream(values).asDoubleStream().toArray();
        final ArrayList<CustomCountdownFormat.CustomFormatField> list = new ArrayList<>(List.of(CustomCountdownFormat.CustomFormatField.values()));

        for (int integer = 0; integer < formatField.getPosition(); integer++) {
            if (tempArrayAdd[integer] <= 0) continue;
            tempArrayAdd[integer + 1] = tempArrayAdd[integer] * list.get(integer + 1).getNextAmount();
        }
        for (int integer = list.size() - 1; integer > formatField.getPosition(); integer--) {
            if (tempArraySub[integer] <= 0) continue;
            tempArraySub[integer - 1] = tempArraySub[integer] / list.get(integer).getNextAmount();
        }
        if (tempArrayAdd[formatField.getPosition()] == tempArraySub[formatField.getPosition()])
            return tempArrayAdd[formatField.getPosition()];
        return tempArrayAdd[formatField.getPosition()] + tempArraySub[formatField.getPosition()];
    }

    private void generateValue() {
        if (values != null) return;
        ArrayList<CustomCountdownFormat.CustomFormatField> fields = new ArrayList<>(List.of(CustomCountdownFormat.CustomFormatField.values()));
        long[] requires = new long[fields.size()];
        for (int field = 0; field < fields.size(); field++) {
            requires[field] = fields.get(field).getNextAmount();
        }

        values = new long[]{0, 0, 0, timeManager.getEpochSecond()};
        boolean negate = (values[values.length - 1] < 0);

        values[values.length - 1] = Math.abs(values[values.length - 1]);
        for (int value = values.length - 1; value > 0; value--) {
            if (!format.getEnabledFields()[value - 1]) {
                break;
            }
            while (values[value] >= requires[value]) {
                values[value] -= requires[value];
                values[value - 1]++;
            }
        }
    }

    public long getSeconds() {
        return getValue(CustomCountdownFormat.CustomFormatField.SECOND);
    }

    public long getMinutes() {
        return getValue(CustomCountdownFormat.CustomFormatField.MINUTE);
    }

    public long getHours() {
        return getValue(CustomCountdownFormat.CustomFormatField.HOUR);
    }

    public long getDays() {
        return getValue(CustomCountdownFormat.CustomFormatField.DAY);
    }
}

