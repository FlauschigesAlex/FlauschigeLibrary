@file:OptIn(CacheInternal::class)
@file:Suppress("unused")

package at.flauschigesalex.lib.base.general

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import kotlin.collections.get

object Cache {
    
    @CacheInternal
    val cache = ConcurrentHashMap<CacheKey<out Any>, CacheEntry<out Any>>()
    
    inline operator fun <reified T: Any> get(any: Any): T? {
        val expired = cache.toList().filter { it.second.isExpired }
        expired.forEach { cache.remove(it.first, it.second) }
        
        val key = any as? CacheKey<*> ?: CacheKey(T::class.java, any)
        
        return cache[key]?.value as? T
    }
    
    inline fun <reified T: Any> getOrDefault(any: Any, value: T): T = get<T>(any) ?: value
    inline fun <reified T: Any> getOrElse(any: Any, value: Supplier<T>): T = get<T>(any) ?: value.get()

    inline fun <reified T: Any> getOrPut(any: Any, value: Supplier<T>): T = get<T>(any) ?: value.get().also {
        this.put<T>(any, it)
    }
    @JvmName("getOrPutEntry")
    inline fun <reified T: Any> getOrPut(any: Any, value: Supplier<CacheEntry<T>>): T = get<T>(any) ?: value.get().value.also {
        this.put<T>(any, value.get())
    }
    
    @JvmName("putEntryTV")
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: Any, reified V: Any> put(any: Any, value: CacheEntry<out V>): T? {
        return this.get<T>(any).also {
            val key = runCatching { any as CacheKey<out Any> }.getOrNull() ?: CacheKey(T::class.java, any)

            cache[key] = value
        }
    }
    @JvmName("putEntryT")
    inline fun <reified T: Any> put(any: Any, value: CacheEntry<out T>): T? = this.put<T, T>(any, value)
    @JvmName("putTV")
    inline fun <reified T: Any, reified V: Any> put(any: Any, value: V): T? = this.put<T, V>(any, CacheEntry(value))
    @JvmName("putT")
    inline fun <reified T: Any> put(any: Any, value: T): T? = this.put<T, T>(any, value)
    
    inline fun <reified T: Any> remove(key: CacheKey<out Any?>): T? {
        this.get<T>(key) ?: return null

        return (cache.remove(key)?.value as? T)
    }
    inline fun <reified T: Any> remove(key: CacheKey<out Any?>, value: CacheEntry<out T>): T? {
        val found = this.get<T>(key) ?: return null

        cache.remove(key, value)
        return found
    }
    
    inline fun <reified T: Any> remove(key: Any): T? {
        val key = key as? CacheKey<*> ?: CacheKey(T::class.java, key)
        return this.remove(key)
    }
    inline fun <reified T: Any> remove(key: Any, value: T): T? {
        val key = key as? CacheKey<*> ?: CacheKey(T::class.java, key)
        return this.remove(key)
    }
    
}

data class CacheKey<T>(val clazz: Class<out T>, val key: Any) {
    override fun equals(other: Any?): Boolean = other is CacheKey<*> && clazz == other.clazz && key == other.key
    override fun hashCode(): Int = clazz.hashCode() * 31 + key.hashCode()
}

data class CacheEntry<T>(val value: T, private val expireRequire: CacheEntry<T>.(T) -> Boolean = { true }) {
    var expiration: Instant = Instant.now().plus(Duration.ofMinutes(10))
    
    val isExpired: Boolean get() = this.isTTLExpired && this.expireRequire(value)
    val isTTLExpired: Boolean get() = expiration.isBefore(Instant.now())
}

@Retention(AnnotationRetention.RUNTIME)
@RequiresOptIn("Internal class", RequiresOptIn.Level.ERROR)
private annotation class CacheInternal