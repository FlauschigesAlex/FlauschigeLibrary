@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.task

class TaskController {

    internal val tasks = ArrayList<Task>()
    fun values(): List<Task> {
        return tasks.toList()
    }

    internal val onTaskEnd = HashMap<Task, ArrayList<() -> Unit>>()
    private var onControllerEnd: () -> Unit = {}

    fun add(task: Task) {
        tasks.add(task)
    }
    fun addAll(taskCollection: Collection<Task>) {
        tasks.addAll(taskCollection)
    }

    fun remove(task: Task) {
        tasks.remove(task)
        this.onTaskStopCallController()
    }
    fun removeAll(taskCollection: Collection<Task>) {
        tasks.removeAll(taskCollection)
        this.onTaskStopCallController()
    }

    internal fun onTaskStopCallController() {
        if (tasks.all { it.status == TaskStatus.STOPPED })
            onControllerEnd.invoke()
    }

    fun stopTask(task: Task, onTaskEnd: (() -> Unit)? = null) {
        if (!tasks.contains(task))
            return

        onTaskEnd?.run {
            this@TaskController.onTaskEnd[task] =
                this@TaskController.onTaskEnd.getOrDefault(task, arrayListOf()).apply {
                    this.add(onTaskEnd)
                }
        }
        task.status = TaskStatus.STOPPING
    }

    fun stopAllTasks(onControllerEnd: (() -> Unit)? = null) {
        tasks.forEach { this.stopTask(it) }
        onControllerEnd?.run { this@TaskController.onControllerEnd = this }
    }
}

class ConsumableTaskController internal constructor(private val controller: TaskController, private val currentTask: Task) {

    fun values(): List<Task> {
        return controller.values()
    }

    fun stopTask(onTaskEnd: (() -> Unit)? = null) {
        return controller.stopTask(currentTask, onTaskEnd)
    }

    fun stopAllTasks(onControllerEnd: (() -> Unit)? = null) {
        return controller.stopAllTasks(onControllerEnd)
    }
}
