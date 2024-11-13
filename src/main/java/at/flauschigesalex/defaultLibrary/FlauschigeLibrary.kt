@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary

import at.flauschigesalex.defaultLibrary.any.MojangAPI
import at.flauschigesalex.defaultLibrary.any.Reflector

@Suppress("unused")
open class FlauschigeLibrary protected constructor() {

    companion object {

        private var flauschigeLibrary: FlauschigeLibrary? = null

        @JvmStatic
        val library: FlauschigeLibrary get() {
            if (flauschigeLibrary == null)
                flauschigeLibrary = FlauschigeLibrary()
            return flauschigeLibrary!!
        }
    }

    var mainThread: Thread
        private set

    init {
        mainThread = Thread.currentThread()
    }

    @Deprecated("MojangAPI was migrated to an object.", level = DeprecationLevel.ERROR)
    val mojangAPI = MojangAPI

    @Deprecated("Reflector was migrated to an object.", level = DeprecationLevel.ERROR)
    val reflector = Reflector
}
