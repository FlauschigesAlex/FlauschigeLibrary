@file:Suppress("unused")

package at.flauschigesalex.defaultLibrary.utils

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