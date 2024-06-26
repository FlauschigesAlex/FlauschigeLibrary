package at.flauschigesalex.defaultLibrary.time.countdown;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public final class CountdownFormat {

    private CountdownFormat(final @Nullable Consumer<CountdownFormat> formatter) {
    }

    public static final class Field {

        public static final Field MILLISECONDS = new Field(TimeUnit.MILLISECONDS, null);

        private Function<Map<TimeUnit, Field>, Boolean> display = (map) -> false;

        private Field(final @NotNull TimeUnit timeUnit, final @Nullable TimeUnit lower) {
        }

        public void display() {
            this.display = (map) -> true;
        }

        public void displayIf(final @NotNull Function<Map<TimeUnit, Field>, Boolean> condition) {
            this.display = condition;
        }
    }
}
