@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package at.flauschigesalex.defaultLibrary

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
