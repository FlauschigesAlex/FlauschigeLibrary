@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary

import at.flauschigesalex.defaultLibrary.any.MojangAPI
import at.flauschigesalex.defaultLibrary.any.Reflector

@Suppress("unused")
open class FlauschigeLibrary protected constructor() {

    companion object {
        @JvmStatic val library = FlauschigeLibrary()
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
