@file:Suppress("unused")

package at.flauschigesalex.lib.base.general

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

private val cache = mutableMapOf<Collection<String>, Reflections>()

@Suppress("MemberVisibilityCanBePrivate")
object Reflector {

    fun reflect(classLoader: ClassLoader, vararg packageNames: String): ReflectionStatement {
        return reflect(classLoader, packageNames.toList())
    }
    fun reflect(classLoader: ClassLoader, packageNames: Collection<String>): ReflectionStatement {
        val set = (if (packageNames.isEmpty()) Package.getPackages().map {
            it.name.split(".").first()
        } else packageNames.toList()).toSet()

        return ReflectionStatement(set, classLoader)
    }
}

class ReflectionStatement internal constructor(private val packageNames: Set<String>, private val loader: ClassLoader) {

    @Suppress("UNCHECKED_CAST")
    fun <C> getSubTypes(clazz: Class<C>, includeSelf: Boolean = false): Set<Class<out C>> {
        val reflections = cache.getOrPut(packageNames) { reflect(packageNames) }

        val found = reflections
            .get(Scanners.SubTypes.of(clazz).asClass<C>(loader))
            .filter { !it.isAnnotationPresent(Invisible::class.java) }
            .toMutableSet()

        if (includeSelf) found += clazz
        return found as Set<Class<out C>>
    }

    fun <A: Annotation> getAnnotatedTypes(annotation: Class<A>, inherit: Boolean = false): Set<Class<*>> {
        if (annotation == Invisible::class.java)
            throw IllegalAccessException("Cannot reflect for invisible annotation.")

        return packageNames.let {
            cache.getOrPut(it) { reflect(it) }
        }.getTypesAnnotatedWith(annotation, !inherit).filter { !it.isAnnotationPresent(Invisible::class.java) }.toSet()
    }

    private fun reflect(packages: Collection<String>): Reflections {

        val urls = packages.flatMap { pkg ->
            ClasspathHelper.forPackage(pkg, loader)
        }.toSet()

        return Reflections(
            ConfigurationBuilder()
                .setUrls(urls)
                .addClassLoaders(loader)
                .addScanners(Scanners.SubTypes, Scanners.TypesAnnotated)
        )
    }
}

/**
 * Returns either kotlin's object instance or tries to create a new instance via (empty) constructor.
 * Else null.
 */
@ExperimentalStdlibApi
fun <A: Any> Class<A>.singleton(debug: Boolean = false): A? {
    try {
        var instance = this.kotlin.objectInstance
        if (instance == null) {
            val constructor = this.getDeclaredConstructor()
            constructor.isAccessible = true
            instance = constructor.newInstance()
        }

        return instance
    } catch (e: Exception) {
        if (debug) e.printStackTrace()
        return null
    }
}