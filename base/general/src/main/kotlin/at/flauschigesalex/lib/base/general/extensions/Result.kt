@file:Suppress("unused")

package at.flauschigesalex.lib.base.general.extensions

fun <T> Result<T>.recoverNotNull(transform: (Throwable) -> T): Result<T> = runCatching { 
    val nullable = this.getOrThrow()!!
    return@runCatching nullable
}.recover(transform)