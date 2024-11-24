@file:Suppress("MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary

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
}
