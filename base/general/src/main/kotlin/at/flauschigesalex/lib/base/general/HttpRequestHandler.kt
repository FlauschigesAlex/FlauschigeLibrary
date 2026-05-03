@file:Suppress("unused")

package at.flauschigesalex.lib.base.general

import kotlinx.coroutines.future.await
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers

@Suppress("UNCHECKED_CAST")
class HttpRequestHandler(val uri: URI, consumer: HttpSettingsConsumer = {}) {
    
    private typealias HttpSettingsConsumer = HttpHandlerSettings.() -> Unit

    constructor(url: URL, consumer: HttpSettingsConsumer = {}) : this(url.toURI(), consumer)
    companion object {
        operator fun invoke(uri: Any, consumer: HttpSettingsConsumer = {}): HttpRequestHandler? =
            runCatching { HttpRequestHandler(URI.create(uri.toString()), consumer) }.getOrNull()
    }

    private val settings = HttpHandlerSettings()

    @Deprecated("Use settings.client instead", ReplaceWith("settings.client"))
    val client: HttpClient
        get() = settings.client
    
    @Deprecated("Use settings.printStackTrace instead", ReplaceWith("settings.printStackTrace"))
    var printStackTrace: Boolean
        get() = settings.printStackTrace
        set(value) { settings.printStackTrace = value }
    
    @Deprecated("Use settings.headers instead", ReplaceWith("settings.headers"))
    val headers: Map<String, Any>
        get() = settings.headers.toMap()
    
    init {
        this.settings.consumer()
    }
    
    @Suppress("DEPRECATION")
    @Deprecated("Use constructor consumer instead", ReplaceWith("HttpRequestHandler(uri, consumer)"))
    fun setHeaders(vararg headers: Pair<String, Any>) = this.setHeaders(headers.toMap())

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor consumer instead", ReplaceWith("HttpRequestHandler(uri, consumer)"))
    fun setHeaders(headers: Map<String, Any>): HttpRequestHandler = this.apply {
        this.settings.headers = headers.toMutableMap()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor consumer instead", ReplaceWith("HttpRequestHandler(uri, consumer)"))
    fun addHeaders(vararg headers: Pair<String, Any>) = this.addHeaders(headers.toMap())
    
    @Suppress("DEPRECATION")
    @Deprecated("Use constructor consumer instead", ReplaceWith("HttpRequestHandler(uri, consumer)"))
    fun addHeaders(headers: Map<String, Any>): HttpRequestHandler = this.apply { 
        this.settings.headers += headers
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use constructor consumer instead", ReplaceWith("HttpRequestHandler(uri, consumer)"))
    fun clearHeaders(): HttpRequestHandler = this.apply { 
        this.settings.headers.clear()
    }
    
    // BEGIN HTTP FUNCTIONS

    suspend fun get(): HttpResponse<String>? = get(BodyHandlers.ofString())
    suspend fun <A: Any> get(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.GET).build()
        return this.run(builder, handler)
    }

    suspend fun post(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = post(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> post(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.POST, publisher).build()
        return this.run(builder, handler)
    }

    suspend fun put(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = put(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> put(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.PUT, publisher).build()
        return this.run(builder, handler)
    }

    suspend fun patch(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = patch(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> patch(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.PATCH, publisher).build()
        return this.run(builder, handler)
    }

    suspend fun delete(): HttpResponse<String>? = delete(BodyHandlers.ofString())
    suspend fun <A: Any> delete(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.DELETE).build()
        return this.run(builder, handler)
    }

    suspend fun options(): HttpResponse<String>? = options(BodyHandlers.ofString())
    suspend fun <A: Any> options(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.OPTIONS).build()
        return this.run(builder, handler)
    }

    private fun prepareBuilder(): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder(uri)
        this.settings.headers.forEach { builder.setHeader(it.key, it.value.toString()) }

        return builder
    }
    
    private suspend fun <A: Any> run(builder: HttpRequest, handler: BodyHandler<A>) = runCatching { 
        this.settings.client.sendAsync(builder, handler).await()
    }.onFailure { if (this.settings.printStackTrace) it.printStackTrace() }.getOrNull()
}

// ADDITIONAL CLASSES

enum class HttpRequestMethod {
  GET, POST, HEAD, PUT, PATCH, DELETE, TRACE, OPTIONS, CONNECT;
}

@ConsistentCopyVisibility
data class HttpHandlerSettings internal constructor(
    var printStackTrace: Boolean = true,
    var headers: MutableMap<String, Any> = mutableMapOf(),
    val client: HttpClient = DEFAULT_CLIENT
) {
    companion object {
        var DEFAULT_CLIENT: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
    }
}

// EXTENSIONS

fun HttpRequest.Builder.method(method: HttpRequestMethod, bodyPublisher: BodyPublisher = BodyPublishers.noBody()): HttpRequest.Builder =
    this.method(method.name, bodyPublisher)