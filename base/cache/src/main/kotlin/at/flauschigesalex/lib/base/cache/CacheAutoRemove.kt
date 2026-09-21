@file:Suppress("unused")

package at.flauschigesalex.lib.base.cache

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

typealias CachePredicate<T> = (CacheEntry<T>) -> Boolean
companion fun <T> CachePredicate.default() = CacheAutoRemove.default<T>()

class CacheAutoRemove<T>(
    private val predicate: CachePredicate<T>
) {
    companion object {
        fun <T> default(): CachePredicate<T> = {
            it.creation + 10.minutes < Clock.System.now()
        }
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal operator fun invoke(entry: CacheEntry<*>, valueClazz: Class<*>): Boolean {
        require(entry.key.valueClass == valueClazz)
        return predicate(entry as CacheEntry<T>)
    }
}

infix fun <T> CachePredicate<T>.and(other: CachePredicate<T>): CachePredicate<T> = { this@and(it) and other(it) }
operator fun <T> CachePredicate<T>.plus(other: CachePredicate<T>): CachePredicate<T> = this.and(other)