@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary

import at.flauschigesalex.defaultLibrary.external.MojangAPI
import at.flauschigesalex.defaultLibrary.reflections.Reflector

@Suppress("unused")
open class FlauschigeLibrary protected constructor() {

    companion object {

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
    }

    val mojangAPI = MojangAPI.access()
    val reflector = Reflector.getReflector()

    fun addWorkingDirectory(path: String): FlauschigeLibrary {
        _workingDirectoryPath.add(path)
        return this
    }
}
