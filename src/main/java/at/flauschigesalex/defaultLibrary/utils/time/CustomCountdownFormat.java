package at.flauschigesalex.defaultLibrary.utils.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings({"unused"})
public final class CustomCountdownFormat {

    public static CustomCountdownFormat defaultDisplay() {
        return new CustomCountdownFormat("d ","h ","m ","s");
    }
    /**
     * @deprecated result is confusing.<br>Recommendation: {@link #defaultDisplay()}.
     */
    public static CustomCountdownFormat emptyDisplay() {
        return new CustomCountdownFormat(emptyStringArray());
    }
    /**
     * @deprecated result is confusing.<br>Recommendation: {@link #defaultDisplay()}.
     */
    public static CustomCountdownFormat customDisplay() {
        return emptyDisplay();
    }
    /**
     * @deprecated result is confusing.<br>Recommendation: {@link #defaultDisplay()}.
     */
    public static CustomCountdownFormat customDisplay(CustomFormatField displayField, String displayText) {
        String[] input = emptyStringArray();
        input[displayField.getPosition()] = displayText;
        return new CustomCountdownFormat(input);
    }
    public static CustomCountdownFormat customDisplay(@NotNull String dayText, @NotNull String hourText, @NotNull String minuteText, @NotNull String secondText) {
        return new CustomCountdownFormat(dayText,hourText,minuteText,secondText);
    }

    private static String[] emptyStringArray() {
        return new String[]{null,null,null,null};
    }

    private final String[] customFields;
    private boolean hideZeroField = false;
    CustomCountdownFormat(String... customFields) {
        this.customFields = customFields;
    }

    public CustomCountdownFormat hideZeroField() {
        hideZeroField = true;
        return this;
    }
    public CustomCountdownFormat field(CustomFormatField field, String fieldText) {
        this.customFields[field.getPosition()] = fieldText;
        return this;
    }
    public CustomCountdownFormat dayField(String fieldText) {
        return field(CustomFormatField.DAY, fieldText);
    }
    public CustomCountdownFormat hourField(String fieldText) {
        return field(CustomFormatField.HOUR, fieldText);
    }
    public CustomCountdownFormat minuteField(String fieldText) {
        return field(CustomFormatField.MINUTE, fieldText);
    }
    public CustomCountdownFormat secondField(String fieldText) {
        return field(CustomFormatField.SECOND, fieldText);
    }

    @Getter @AllArgsConstructor
    public enum CustomFormatField {
        DAY(1), HOUR(24), MINUTE(60), SECOND(60);

        private final int nextAmount;

        public int getPosition() {
            ArrayList<CustomFormatField> fields = new ArrayList<>(List.of(CustomFormatField.values()));
            for (int field = 0; field < fields.size(); field++) {
                if (fields.get(field) == this) return field;
            }
            return -1;
        }
    }
}
