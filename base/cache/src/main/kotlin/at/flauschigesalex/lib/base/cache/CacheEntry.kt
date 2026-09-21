package at.flauschigesalex.lib.base.cache

import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant

data class CacheEntry<T>(
    val key: CacheIdentifier<*>,
    val value: T
) {
    companion object;
    
    @Transient val creation: Instant = Clock.System.now()
    @Transient var autoRemove: CacheAutoRemove<T>? = null
}
