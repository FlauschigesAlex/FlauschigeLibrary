@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.task

import at.flauschigesalex.defaultLibrary.task.Task.Companion.avoidAsyncStackOverflow
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class Task private constructor(private val controller: TaskController, private val function: (ConsumableTaskController?) -> Unit, private val async: Boolean) {

    companion object {
        /**
         * All tasks throw an [exception][StackOverflowError] after 2k-4k executions.
         * To avoid this, [async tasks][Task.createAsyncTask] will create a [new executor][ExecutorService] every 3000 executions.
         * [Regular tasks][Task.createTask] will still throw an exception unless limited or manually stopped.
         * @see StackOverflowError
         */
        @JvmStatic var avoidAsyncStackOverflow = true

        /**
         * Delayed tasks don't sleep for the entire [TaskDelay] at once, instead they check every `value` milliseconds if it was ordered to stop.
         * If so, the task stops instantly. This value is especially important when working with synchronized tasks.
         * To improve performance, increase `value`, to improve accuracy, decrease `value`.
         * Ranges from 50 ms to 3600000 (1h)
         */
        @JvmStatic var sleepFractionCheckStop: Long = 50
            set(value) {
                field = min(sleepFractionCheckStopMAX, max(sleepFractionCheckStopMIN, value))
            }

        @JvmStatic val sleepFractionCheckStopMIN: Long = 50
        @JvmStatic val sleepFractionCheckStopMAX: Long = TimeUnit.HOURS.toMillis(1)

        /**
         * Creates a new task in the thread it was created in.
         */
        @JvmStatic fun createTask(controller: TaskController? = null, function: (ConsumableTaskController?) -> Unit): Task {
            return Task(controller ?: TaskController(), function, false)
        }

        /**
         * Creates a new task in a new [executor][ExecutorService] that will not interfere with the thread it was created in.
         * @see Thread
         * @see [ExecutorService]
         */
        @JvmStatic fun createAsyncTask(controller: TaskController? = null, function: (ConsumableTaskController?) -> Unit): Task {
            return Task(controller ?: TaskController(), function, true)
        }
    }

    init {
        controller.add(this)
    }

    var status: TaskStatus = TaskStatus.UNDEFINED
        internal set
    var isRepeating: Boolean = false
        private set
    val isStopping get() = status == TaskStatus.STOPPING || status == TaskStatus.STOPPED

    private var asyncThread: ExecutorService? = null
    private var newAsyncThread = false
    private var executed = false

    var totalExecutions = 0
        private set

    fun execute() {
        this.execute(1, null)
    }

    /**
     * @param duration The duration to wait before executing the task.
     */
    fun executeDelayed(duration: Duration, controllerConsumer: ((ConsumableTaskController) -> Unit)? = null) {
        this.execute(1, TaskDelay(duration, TaskDelayType.ONLY_START))
    }

    /**
     * @throws StackOverflowError
     * @see avoidAsyncStackOverflow
     * @see Task.createAsyncTask
     */
    fun repeat(controllerConsumer: ((ConsumableTaskController) -> Unit)? = null) {
        return this.repeat(null, controllerConsumer)
    }
    /**
     * @throws StackOverflowError
     * @see avoidAsyncStackOverflow
     * @see Task.createAsyncTask
     */
    fun repeat(repeatTimes: Int? = null, controllerConsumer: ((ConsumableTaskController) -> Unit)? = null) {
        this.execute(repeatTimes, null)
    }

    /**
     * @param delay The duration to wait before executing the task.
     * @throws StackOverflowError
     * @see avoidAsyncStackOverflow
     * @see Task.createAsyncTask
     */
    fun repeatDelayed(delay: TaskDelay) {
        this.repeatDelayed(null, delay)
    }
    fun repeatDelayed(repeatTimes: Int? = null, delay: TaskDelay) {
        this.execute(repeatTimes, delay)
    }

    private fun execute(executeTimes: Int?, delay: TaskDelay?, isFirstExecution: Boolean = true, internalFirstStart: Boolean = true) {
        if (status == TaskStatus.STOPPING)
            return this.stopTask()

        if (internalFirstStart) {
            if (executed)
                throw IllegalStateException("Tasks cannot be executed twice.")

            this.executed = true
            this.isRepeating = executeTimes == null || executeTimes > 1
        }

        if (status == TaskStatus.UNDEFINED && isFirstExecution)
            status = TaskStatus.RUNNING

        if (status == TaskStatus.STOPPING)
            return this.stopTask()

        if (executeTimes != null && executeTimes <= 0)
            return this.stopTask()

        if (async) {
            if (asyncThread == null || (totalExecutions % 3000 == 0 && !newAsyncThread && avoidAsyncStackOverflow)) {
                this.asyncThread = Executors.newCachedThreadPool()
                newAsyncThread = true
                this.asyncThread!!.execute {
                    this.execute(executeTimes, delay, isFirstExecution, false)
                }
                return
            } else newAsyncThread = false
        }

        val ctc = ConsumableTaskController(controller, this@Task)

        if (status == TaskStatus.STOPPING)
            return this.stopTask()

        delay?.run {
            if (!this.delayType.shouldDelay(isFirstExecution) || !this.duration.isPositive)
                return@run

            var sleep = this.duration.toMillis()
            while (sleep > 0) {
                val sleepFraction = min(sleep, sleepFractionCheckStop)

                if (status == TaskStatus.STOPPING)
                    return stopTask()

                Thread.sleep(sleepFraction)
            }
        }

        if (status == TaskStatus.STOPPING)
            return this.stopTask()

        if (status == TaskStatus.RUNNING) {
            function.invoke(if (isRepeating) ctc else null)
            totalExecutions++
        } else return this.stopTask()

        try {
            this.execute(executeTimes?.minus(1), delay, false, false)
        } catch (fail: Exception) {
            fail.printStackTrace()
            this.stopTask()
        }
    }

    private fun stopTask() {
        this.asyncThread?.shutdownNow()
        status = TaskStatus.STOPPED
        controller.onTaskStopCallController()
    }
}

data class TaskDelay(val duration: Duration, val delayType: TaskDelayType = TaskDelayType.ONLY_BETWEEN)

enum class TaskStatus {
    UNDEFINED,
    RUNNING,
    STOPPING,
    STOPPED,
    ;
}
enum class TaskDelayType {
    ALWAYS,
    ONLY_START,
    ONLY_BETWEEN,
    ;

    internal fun shouldDelay(isFirst: Boolean): Boolean {
        return when (this) {
            ONLY_START -> isFirst
            ONLY_BETWEEN -> isFirst.not()

            else -> true
        }
    }
}