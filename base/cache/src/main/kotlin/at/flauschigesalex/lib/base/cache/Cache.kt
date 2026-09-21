package at.flauschigesalex.lib.base.cache

import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object Cache {
    @PublishedApi
    internal val cache: MutableSet<CacheEntry<*>> = ConcurrentHashMap.newKeySet()

    @PublishedApi
    internal fun removeInvalid() {
        cache.removeIf {
            it.autoRemove?.invoke(it, it.key.valueClass) ?: false
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified A> find(
        predicate: (CacheEntry<A?>) -> Boolean
    ): Set<CacheEntry<A?>> {
        this.removeInvalid()
        
        return cache.filter { it.key.valueClass == A::class.java }
            .filter { predicate(it as CacheEntry<A?>) }
            .toSet() as Set<CacheEntry<A?>>
    }

    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified T> get(identifier: CacheIdentifier<T>): T? {
        this.removeInvalid()

        val entry = this.cache.find {
            it.key == identifier
        } as CacheEntry<T>?

        return entry?.value
    }

    inline fun <reified T> getOrDefault(identifier: CacheIdentifier<T>, default: T): T =
        this[identifier] ?: default

    inline fun <reified T> getOrElse(identifier: CacheIdentifier<T>, supplier: () -> T): T =
        this[identifier] ?: supplier()

    inline fun <reified T, V : T?> getOrPut(identifier: CacheIdentifier<T>, supplier: () -> V, consumer: (CacheEntry<V>) -> Unit = {}): T? =
        this[identifier] ?: supplier().also { putIfAbsent(identifier, it, consumer) }

    inline fun <reified T, V : T?> put(identifier: CacheIdentifier<T>, value: V, consumer: (CacheEntry<V>) -> Unit = {}): T? {
        cache.removeIf { it.key == identifier }

        return putIfAbsent(identifier, value, consumer)
    }

    inline fun <reified T, V : T?> putIfAbsent(identifier: CacheIdentifier<T>, value: V, consumer: (CacheEntry<V>) -> Unit = {}): T? {
        val existing = this[identifier]
        if (existing != null)
            return existing

        val entry = CacheEntry(identifier, value)
        consumer(entry)
        if (entry.autoRemove?.invoke(entry, T::class.java) ?: false)
            return existing

        cache.add(entry)
        return existing
    }

    fun <T> remove(identifier: CacheIdentifier<T>) {
        cache.removeIf { it.key == identifier }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified A> removeIf(
        predicate: (CacheEntry<A?>) -> Boolean
    ): Int {
        val amount: Int

        this.find<A> { predicate(it) }
            .also { amount = it.count() }
            .forEach { cache.remove(it) }

        return amount
    }
}