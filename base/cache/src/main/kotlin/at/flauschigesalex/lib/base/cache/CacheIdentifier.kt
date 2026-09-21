@file:Suppress("unused")

package at.flauschigesalex.lib.base.cache

@ConsistentCopyVisibility
data class CacheIdentifier<T> @PublishedApi internal constructor(
    val valueClass: Class<T>,
    val key: Any,
) {
    companion object {
        inline operator fun <reified T> invoke(
            key: Any
        ): CacheIdentifier<T> = CacheIdentifier(T::class.java, key)
    }

    override fun equals(other: Any?): Boolean = other is CacheIdentifier<*> && other.key == key
    override fun hashCode(): Int = key.hashCode()
}

@Deprecated("Use CacheIdentifier instead", ReplaceWith("CacheIdentifier<T>"))
typealias CacheKey<T> = CacheIdentifier<T>