@file:Suppress("unused")

package at.flauschigesalex.lib.base.general.extensions

import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

suspend fun <T> CompletableFuture<T>.awaitCatching(): Result<T> = runCatching { await() }