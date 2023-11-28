@file:Suppress("unused")

package at.flauschigesalex.lib.base.general

import kotlinx.coroutines.*
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class Promise<T> {
    companion object {
        fun <T> empty(): Promise<T?> = Promise(null)
    }

    private var job: Job? = null
    private var scope: CoroutineScope? = null

    internal val deferred: Deferred<T>
    private var result: T? = null
    
    constructor(dispatcher: CoroutineDispatcher = Dispatchers.Default, supplier: suspend () -> T) {
        this.job = SupervisorJob()
        this.scope = CoroutineScope(job!! + dispatcher)

        this.deferred = scope!!.async { supplier() }
        this.init()
    }
    constructor(result: T) {
        this.result = result
        this.deferred = CompletableDeferred(result)
        this.init()
    }
    
    private fun init() {
        deferred.invokeOnCompletion { 
            val value = deferred.getCompleted()
            result = value
            callbacks.forEach { it(value) }
            job?.cancel()
        }
    }
    
    val isCompleted: Boolean
        get() = deferred.isCompleted
    
    fun orElse(other: T): T = result ?: other
    fun getOrElse(supplier: () -> T): T = result ?: supplier()
    
    fun get(): Deferred<T> = deferred
    fun getOrNull(): T? = result
    fun getOrThrow(): T = result!!
    
    private val callbacks = mutableListOf<(T) -> Unit>()
    fun onSuccess(block: (T) -> Unit) = apply { await(block) }
    fun await(block: (T) -> Unit) {
        result?.run { 
            return block(this)
        }
        
        callbacks.add(block)
    }
}

fun <T> Iterable<Promise<T>>.awaitAll(block: (List<T>) -> Unit) {
    val status = this.associateWith { false }.toMutableMap()
    status.forEach { (promise, _) -> 
        promise.await { status[promise] = true }
        if (status.all { it.value })
            block(status.map { it.key.getOrThrow() })
    }
}

fun <T> Iterable<Promise<T>>.flatten(): Promise<List<T>> = Promise {
    this.map { it.deferred }.awaitAll()
}

fun <T> Promise<out Iterable<T?>>.filterNotNull(): Promise<List<T>> = Promise { this.deferred.await().filterNotNull() }
fun <T> Promise<out Iterable<T>>.filter(predicate: (T) -> Boolean): Promise<List<T>> =
    this.map { it.filter(predicate) }
fun <T> Promise<out Iterable<T>>.filterNot(predicate: (T) -> Boolean): Promise<List<T>> =
    this.map { it.filterNot(predicate) }

fun <T> Promise<out Iterable<T>>.find(predicate: (T) -> Boolean): Promise<T?> = this.map { it.find(predicate) }

fun <T, R> Promise<T>.map(transform: (T) -> R): Promise<R> =
    Promise { transform(this.deferred.await()) }
fun <T, R> Promise<T>.mapPromise(transform: (T) -> Promise<R>): Promise<R> =
    Promise { transform(this.deferred.await()).deferred.await() }