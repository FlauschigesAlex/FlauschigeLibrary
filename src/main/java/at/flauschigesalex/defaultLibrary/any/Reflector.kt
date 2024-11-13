@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package at.flauschigesalex.defaultLibrary.any

import at.flauschigesalex.defaultLibrary.utils.Invisible
import at.flauschigesalex.defaultLibrary.utils.LibraryException
import org.reflections.Reflections

private val cache = mutableMapOf<String, Reflections>()

object Reflector {

    fun reflect(vararg packageNames: String): ReflectionStatement {
        return reflect(packageNames.toList())
    }
    fun reflect(packageNames: Collection<String>): ReflectionStatement {
        val set = (if (packageNames.isEmpty()) Package.getPackages().map {
            it.name.split(".").first()
        } else packageNames.toList()).toSet()

        return ReflectionStatement(set)
    }
}

class ReflectionStatement internal constructor(private val packageNames: Set<String>) {

    fun <C> getSubTypes(clazz: Class<C>, includeSelf: Boolean = false): Set<Class<out C>> {
        val set = HashSet<Class<out C>>()

        if (includeSelf)
            set.add(clazz)

        packageNames.forEach {
            if (it.isEmpty())
                return@forEach

            cache.putIfAbsent(it, Reflections(it))
            set.addAll(cache[it]!!.getSubTypesOf(clazz)
                .filter { !it.isAnnotationPresent(Invisible::class.java) })
        }

        return set
    }

    fun <A: Annotation> getAnnotatedTypes(annotation: Class<A>, inherit: Boolean = false): Set<Class<*>> {
        if (annotation == Invisible::class.java)
            throw LibraryException("Cannot reflect for invisible annotation.")

        val set = HashSet<Class<*>>()

        packageNames.forEach {
            cache.putIfAbsent(it, Reflections(it))
            set.addAll(cache[it]!!.getTypesAnnotatedWith(annotation, !inherit)
                .filter { !it.isAnnotationPresent(Invisible::class.java) })
        }

        return set
    }
}