@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package at.flauschigesalex.defaultLibrary

fun Class<*>.supertypes(includeInterfaces: Boolean = false): List<Class<*>> {
    val set = hashSetOf<Class<*>>()
    var current = this

    while (true) {
        if (current != this)
            set.add(current)

        if (includeInterfaces)
            set.addAll(current.interfaces)

        if (current == Any::class.java)
            break

        current = current.superclass
    }

    return set.toList()
}

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
