@file:Suppress("unused")

package at.flauschigesalex.lib.base.general.reflect

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass

private val cache = mutableMapOf<Collection<String>, Reflections>()

@Suppress("MemberVisibilityCanBePrivate")
object Reflector {
    fun reflect(loader: ClassLoader, vararg packageNames: String) = reflect(loader, packageNames.toList())
    fun reflect(loader: ClassLoader, packageNames: Collection<String>): ReflectionStatement {
        val topLevelPackages = loader.definedPackages.map { 
            it.name.substringBefore('.')
        }.toSet()
        
        return ReflectionStatement(topLevelPackages, loader)
    }
}

@ConsistentCopyVisibility
data class ReflectionStatement internal constructor(
    internal val packageNames: Set<String>,
    private val loader: ClassLoader
) {
    
    inline fun <reified C: Any> subTypesOf() = subTypesOf(C::class)
    fun <C: Any> subTypesOf(javaClass: Class<C>, self: Boolean = false) = subTypesOf(javaClass.kotlin, self)
    fun <C: Any> subTypesOf(kotlinClass: KClass<C>, self: Boolean = false): Set<Class<out C>> {
        val reflections = cache.getOrPut(packageNames) { reflect(packageNames) }
        
        val found = reflections.get(Scanners.SubTypes.of(kotlinClass.java).asClass<C>(loader))
            .filterNot { it.isAnnotationPresent(Invisible::class.java) }
            .filterIsInstance<Class<out C>>()
            .toMutableSet()
        
        if (self) found += kotlinClass.java
        return found
    }

    @Deprecated("Use subTypesOf", ReplaceWith("subTypesOf(clazz, includeSelf)"))
    fun <C> getSubTypes(clazz: Class<C>, includeSelf: Boolean = false): Set<Class<out C>> {
        val a = clazz as Class<out C>
        return subTypesOf(a, includeSelf)
    }
    
    inline fun <reified A: Annotation> annotatedTypes() = annotatedTypes(A::class)
    fun <A: Annotation> annotatedTypes(annotationJavaClass: Class<A>, inherit: Boolean = false) = annotatedTypes(annotationJavaClass.kotlin, inherit)
    fun <A: Annotation> annotatedTypes(annotationKotlinClass: KClass<A>, inherit: Boolean = false): Set<Class<*>> {
        if (annotationKotlinClass == Invisible::class) error("Cannot reflect for invisible annotation.")
        
        val found = cache.getOrPut(packageNames) { reflect(packageNames) }
        return found.getTypesAnnotatedWith(annotationKotlinClass.java, inherit)
            .filterNot { it.isAnnotationPresent(Invisible::class.java) }
            .toSet()
    }

    @Deprecated("Use annotatedTypes", ReplaceWith("annotatedTypes(annotation, inherit)"))
    fun <A: Annotation> getAnnotatedTypes(annotation: Class<A>, inherit: Boolean = false): Set<Class<*>> = annotatedTypes(annotation, inherit)

    fun reflect(packages: Collection<String>, config: ConfigurationBuilder? = null): Reflections {
        val urls = packages.flatMap { ClasspathHelper.forPackage(it, loader) }.toSet()
        
        val config = config ?: ConfigurationBuilder()
            .setUrls(urls)
            .addClassLoaders(loader)
            .addScanners(*Scanners.entries.toTypedArray())

        return Reflections(config)
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