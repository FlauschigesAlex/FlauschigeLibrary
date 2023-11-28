@file:Suppress("unused")

package at.flauschigesalex.lib.base.general

import kotlinx.coroutines.future.await
import org.jetbrains.annotations.Unmodifiable
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
class HttpRequestHandler private constructor(val uri: URI) {

    companion object {
        operator fun invoke(url: URL): HttpRequestHandler = HttpRequestHandler(url.toURI())
        operator fun invoke(uri: URI): HttpRequestHandler = HttpRequestHandler(uri)
        operator fun invoke(url: Any): HttpRequestHandler? {
            try {
                val uri = URI.create(url.toString())
                return HttpRequestHandler(uri)
            } catch (_: Exception) {
            }
            return null
        }

        @Deprecated("", level = DeprecationLevel.ERROR)
        fun <A: Any> request(url: Any,
                             method: HttpRequestMethod = HttpRequestMethod.GET,
                             handler: BodyHandler<A> = BodyHandlers.ofString() as BodyHandler<A>,
                             headers: Map<String, Any> = HashMap(),
                             data: Any? = null
        ): HttpResponse<A> = throw IllegalAccessException()
    }

    val client: HttpClient
        get() = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

    var printStackTrace = true

    private val _headers = HashMap<String, Any>()
    val headers: @Unmodifiable Map<String, Any>
        get() = _headers.toMap()

    fun setHeaders(vararg headers: Pair<String, Any>) = this.setHeaders(headers.toList().toMap())
    fun setHeaders(headers: Map<String, Any>): HttpRequestHandler {
        this.clearHeaders()
        this.addHeaders(headers)
        return this
    }
    fun addHeaders(vararg headers: Pair<String, Any>) = this.addHeaders(headers.toList().toMap())
    fun addHeaders(headers: Map<String, Any>): HttpRequestHandler {
        this._headers.putAll(headers)
        return this
    }
    fun clearHeaders(): HttpRequestHandler {
        this._headers.clear()
        return this
    }

    suspend fun get(): HttpResponse<String>? = get(BodyHandlers.ofString())
    suspend fun <A: Any> get(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.GET).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    suspend fun post(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = post(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> post(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.POST, publisher).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    suspend fun put(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = put(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> put(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.PUT, publisher).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    suspend fun patch(publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<String>? = patch(BodyHandlers.ofString(), publisher)
    suspend fun <A: Any> patch(handler: BodyHandler<A>, publisher: BodyPublisher = BodyPublishers.noBody()): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.PATCH, publisher).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    suspend fun delete(): HttpResponse<String>? = delete(BodyHandlers.ofString())
    suspend fun <A: Any> delete(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.DELETE).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    suspend fun options(): HttpResponse<String>? = options(BodyHandlers.ofString())
    suspend fun <A: Any> options(handler: BodyHandler<A>): HttpResponse<A>? {
        val builder = prepareBuilder().method(HttpRequestMethod.OPTIONS).build()
        try {
            return client.sendAsync(builder, handler).await()
        } catch (e: Exception) {
            if (printStackTrace) e.printStackTrace()
        }

        return null
    }

    private fun prepareBuilder(): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder(uri)
        this._headers.forEach { builder.setHeader(it.key, it.value.toString()) }

        return builder
    }
}

fun HttpRequest.Builder.method(method: HttpRequestMethod, bodyPublisher: BodyPublisher = BodyPublishers.noBody()): HttpRequest.Builder = this.method(method.name, bodyPublisher)

enum class HttpRequestMethod {
  GET, POST, HEAD, PUT, PATCH, DELETE, TRACE, OPTIONS, CONNECT;
}