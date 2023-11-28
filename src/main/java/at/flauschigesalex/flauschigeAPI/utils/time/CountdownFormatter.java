package at.flauschigesalex.flauschigeAPI.utils.time;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray", "UnusedReturnValue"})
public final class CountdownFormatter {

    private final TimeManagerLimited timeManager;
    private final Countdown countdown;
    StringBuilder builder = new StringBuilder();

    @Getter(AccessLevel.PROTECTED)
    private final boolean[] enabledFields = new boolean[]{false,false,false,false};
    private final String[] fieldCodes = new String[]{"day","hour","minute","second"};

    CountdownFormatter(TimeManagerLimited timeManager, Countdown formatter) {
        this.timeManager = timeManager;
        this.countdown = formatter;
    }
    
    public CountdownFormatter clear() {
        Arrays.fill(this.enabledFields, false);
        this.builder = new StringBuilder();
        return this;
    }
    public CountdownFormatter append(String append) {
        this.builder.append(append);
        return this;
    }
    CountdownFormatter field(int field) {
        return field(field, "");
    }
    CountdownFormatter field(int field, String append) {
        this.enabledFields[field] = true;
        this.builder.append("{").append(fieldCodes[field]).append("}").append(append);
        return this;
    }
    public CountdownFormatter dayField() {
        return this.field(0);
    }
    public CountdownFormatter dayField(String append) {
        return this.field(0, append);
    }
    public CountdownFormatter hourField() {
        return this.field(1);
    }
    public CountdownFormatter hourField(String append) {
        return this.field(1, append);
    }
    public CountdownFormatter minuteField() {
        return this.field(2);
    }
    public CountdownFormatter minuteField(String append) {
        return this.field(2, append);
    }
    public CountdownFormatter secondField() {
        return this.field(3);
    }
    public CountdownFormatter secondField(String append) {
        return this.field(3, append);
    }

    @Nullable FieldAccessor byID(int id) {
        try {
            return new ArrayList<>(List.of(FieldAccessor.values())).get(id);
        } catch (Exception fail) {
            return null;
        }
    }
    enum FieldAccessor {
        DAY, HOUR, MINUTE, SECOND
    }

    public Countdown create() {
        this.countdown.format = this;
        return this.countdown;
    }
}
