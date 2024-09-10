@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary

import at.flauschigesalex.defaultLibrary.external.MojangAPI
import at.flauschigesalex.defaultLibrary.project.ProjectManager
import at.flauschigesalex.defaultLibrary.project.task.Task
import at.flauschigesalex.defaultLibrary.reflections.Reflector
import java.lang.reflect.Constructor

@Suppress("unused")
open class FlauschigeLibrary protected constructor() {

    companion object {

        var autoRegisterManagers: Boolean = true
        private var flauschigeLibrary: FlauschigeLibrary? = null

        @JvmStatic fun main(args: Array<String>) {
            library
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         * @return an instance of the Library
         */
        @JvmStatic val library: FlauschigeLibrary get() {
            if (flauschigeLibrary == null)
                flauschigeLibrary = FlauschigeLibrary()
            return flauschigeLibrary!!
        }

        /**
         * Make sure to run this method in your main class!
         * This is extremely important for reflections!
         *
         * @return an instance of the Library
         */
        fun getLibrary(autoRegisterManagers: Boolean = true): FlauschigeLibrary {
            Companion.autoRegisterManagers = autoRegisterManagers
            return library
        }
    }

    val ownDirectoryPath: String = javaClass.getPackage().name
    private val _workingDirectoryPath = ArrayList<String>()
    val workingDirectoryPath get() = ArrayList(_workingDirectoryPath)

    var mainThread: Thread
        private set

    init {
        packages@ for (definedPackage in javaClass.classLoader.definedPackages) {
            if (definedPackage.name.startsWith(ownDirectoryPath)) continue

            for (workingDirectory in this._workingDirectoryPath)
                if (definedPackage.name.startsWith(workingDirectory))
                    continue@packages

            _workingDirectoryPath.add(definedPackage.name)
        }

        mainThread = Thread.currentThread()
        if (autoRegisterManagers) executeManagers()
    }

    fun executeManagers() {
        val managers = ArrayList<ProjectManager<*>>()
        for (subClass in reflector.reflect(
            ownDirectoryPath,
            *_workingDirectoryPath.toArray(arrayOf())
        ).getSubClasses(ProjectManager::class.java)) {
            try {
                val constructor: Constructor<*> = subClass.getDeclaredConstructor()
                constructor.isAccessible = true
                managers.add(constructor.newInstance() as ProjectManager<*>)
            } catch (ignore: Exception) {
            }
        }
        managers.sortWith(ProjectManager.comparator().reversed())

        Task.createAsyncTask { _ -> managers.forEach { manager: ProjectManager<*> ->
                try {
                    if (manager.executeManager(this)) {
                        if (manager.successMessage() != null) println(manager.successMessage())
                    } else {
                        if (manager.failMessage() != null) println(manager.failMessage())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } }.execute()
    }

    fun addWorkingDirectory(path: String): FlauschigeLibrary {
        _workingDirectoryPath.add(path)
        return this
    }

    val mojangAPI: MojangAPI
        get() = MojangAPI.access()

    val reflector: Reflector
        get() = Reflector.getReflector()
}
