package at.flauschigesalex.defaultLibrary.task;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

@SuppressWarnings({"unused"})
public final class Task {

    private static @Getter long totalTaskCount = 0;
    public static int default_delay_ms = 250;

    public static @CheckReturnValue Task createTask(final @NotNull Consumer<Optional<Controller>> consumer) {
        return new Task(consumer, false);
    }
    public static @CheckReturnValue Task createTask(final @NotNull Consumer<Optional<Controller>> consumer, final @NotNull Controller controller) {
        return new Task(consumer, false, controller);
    }

    public static @CheckReturnValue Task createAsyncTask(final @NotNull Consumer<Optional<Controller>> consumer) {
        return new Task(consumer, true);
    }
    public static @CheckReturnValue Task createAsyncTask(final @NotNull Consumer<Optional<Controller>> consumer, final @NotNull Controller controller) {
        return new Task(consumer, true, controller);
    }

    private final Consumer<Optional<Controller>> consumer;
    private final @Getter boolean async;
    private ThreadPoolExecutor executor;

    private @Getter Controller controller;
    private final @Getter Thread parentThread;

    private int resets = 0;
    private int executed = 0;
    private Boolean repeating;

    private boolean stopped;

    private Task(final @NotNull Consumer<Optional<Controller>> consumer, final boolean async) {
        this(consumer, async, Controller.create());
    }

    private Task(final @NotNull Consumer<Optional<Controller>> consumer, final boolean async, final @NotNull Controller controller) {
        this.consumer = consumer;
        this.async = async;

        this.controller = controller;
        this.controller.addTask(this);

        this.parentThread = FlauschigeLibrary.getLibrary().getMainThread();

        totalTaskCount++;
    }

    public void execute() {
        this.run(1L, null, 0, null, false);
    }

    public void executeDelayed(final @NotNull TimeUnit unit, long value) {
        this.run(1L, unit, value, null, false);
    }

    public void repeat() {
        this.run(null, null, 0, null, true);
    }

    public void repeat(final long amount) {
        this.run(amount, null, 0, null, true);
    }

    public void repeatDelayed(final @NotNull TimeUnit unit, long value) {
        this.run(null, unit, value, TaskDelayType.defaultType(), true);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeUnit unit, long value) {
        this.run(amount, unit, value, TaskDelayType.defaultType(), true);
    }

    public void repeatDelayed(final @NotNull TimeUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(null, unit, value, type, true);
    }

    public void repeatDelayed(final long amount, final @NotNull TimeUnit unit, long value, final @NotNull TaskDelayType type) {
        this.run(amount, unit, value, type, true);
    }

    @SneakyThrows
    private void run(final @Nullable Long amount, final @Nullable TimeUnit unit, long value, final @Nullable TaskDelayType type, final boolean first) {
        long valueMilli = unit != null ? unit.toMillis(value) : -1;

        if (unit != null)
            if (unit == TimeUnit.MICROSECONDS || unit == TimeUnit.NANOSECONDS)
                throw new IllegalArgumentException(unit+" is to little to be a delay, try "+TimeUnit.MILLISECONDS+" or larger.");

        if (amount != null && amount <= 0) {
            if (executor != null)
                executor.shutdown();

            stopped = true;
            if(controller != null)
                controller.runExit(this);
            return;
        }

        if (repeating == null) {
            repeating = first;
            if (!repeating)
                controller = null;
        }

        if (async && executor == null || executed > 2000) {
            executed = 0;
            resets++;

            if (executor != null)
                executor.shutdownNow();

            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            executor.execute(() -> this.run(amount, unit, value, type, first));
            return;
        }

        if (this.checkStop())
            return;

        if (unit != null)
            while (valueMilli > 0) {
                if (type == TaskDelayType.ONLY_BETWEEN && first)
                    break;
                if (type == TaskDelayType.ONLY_BEGINNING && !first)
                    break;

                final var removeValue = Math.min(valueMilli, default_delay_ms);
                valueMilli -= removeValue;
                sleep(removeValue);

                if (this.checkStop())
                    return;
            }

        consumer.accept(Optional.ofNullable(this.controller));
        this.executed++;
        this.run(amount != null ? amount - 1 : null, unit, value, type, false);
    }

    public boolean hasStopped() {
        return stopped;
    }

    public long getTotalExecutions() {
        return executed+(resets*2000L);
    }

    private boolean checkStop() {
        if (this.controller == null)
            return false;

        if (!this.controller.shouldStop)
            return false;

        if (executor != null)
            executor.shutdownNow();

        stopped = true;
        controller.runExit(this);
        return true;
    }

    @Getter
    public static final class Controller {

        private static @Getter long totalControllerCount;
        private static final ArrayList<Controller> controllerList = new ArrayList<>();

        public static List<Controller> getControllersByName(final @NotNull String name, final boolean ignoreCase) {
            return controllerList.stream().filter(controller -> {
                if (ignoreCase)
                    return controller.name.equalsIgnoreCase(name);
                return controller.name.equals(name);
            }).toList();
        }

        /**
         * By using a self-created controller, you can group multiple tasks together.
         */
        @ApiStatus.Experimental
        public static Controller create() {
            return create("TaskController #"+totalControllerCount);
        }

        /**
         * By using a self-created controller, you can group multiple tasks together.
         * @param name The TaskController's name.
         */
        @ApiStatus.Experimental
        public static Controller create(final @NotNull String name) {
            return new Controller(name);
        }

        private final ArrayList<Task> taskList = new ArrayList<>();
        private final String name;

        private boolean shouldStop;
        private Runnable afterStop;

        private Controller(final @NotNull String name, final @NotNull Task task) {
            this(name);
            this.addTask(task);
        }
        private Controller(final @NotNull String name) {
            this.name = name;

            controllerList.add(this);
            totalControllerCount++;
        }

        private void addTask(final @NotNull Task task) {
            this.taskList.add(task);
        }

        /**
         * Stops all {@link #taskList tasks} belonging to this controller.<br>
         * May take up to 250ms unless {@link #default_delay_ms} is modified.
         */
        public void stop() {
            if (taskList.isEmpty())
                throw new IllegalStateException("Controller cannot be stopped since it contains no tasks.");

            if (shouldStop)
                throw new IllegalStateException("Controller is already marked as stopping.");

            if (hasStopped())
                throw new IllegalStateException("Controller or its tasks have already stopped.");


            shouldStop = true;
        }

        /**
         * Stops all {@link #taskList tasks} belonging to this controller.<br>
         * May take up to 250ms unless {@link #default_delay_ms} is modified.
         * @param after Code executed after all tasks have stopped.
         */
        public void awaitStop(final @NotNull Runnable after) {
            this.afterStop = after;
            stop();
        }

        /**
         * Stops all {@link #taskList tasks} belonging to this controller <i>if the condition is true</i>.<br>
         * May take up to 250ms unless {@link #default_delay_ms} is modified.
         */
        public void stopIf(final @NotNull BooleanSupplier condition) {
            if (!condition.getAsBoolean())
                return;

            stop();
        }

        /**
         * Stops all {@link #taskList tasks} belonging to this controller <i>if the condition is true</i>.<br>
         * May take up to 250ms unless {@link #default_delay_ms} is modified.
         * @param after Code executed after all tasks have stopped.
         */
        public void awaitStopIf(final @NotNull BooleanSupplier condition, final @NotNull Runnable after) {
            if (!condition.getAsBoolean())
                return;

            this.awaitStop(after);
        }

        private void runExit(final @NotNull Task ending) {
            if (!hasStopped() || afterStop == null)
                return;

            afterStop.run();
        }

        public boolean hasStopped() {
            return taskList.stream().allMatch(Task::hasStopped);
        }

        public @Deprecated boolean isShouldStop() {
            return shouldStop;
        }
    }
}
