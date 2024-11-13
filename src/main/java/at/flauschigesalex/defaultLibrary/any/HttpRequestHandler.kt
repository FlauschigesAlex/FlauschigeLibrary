package at.flauschigesalex.defaultLibrary.any

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Suppress("unused", "MemberVisibilityCanBePrivate")
object HttpRequestHandler {
    fun get(url: Any, headers: Map<String, Any> = mapOf()): HttpResponse<String> {
        return get(URI(url.toString()), headers)
    }
    fun get(uri: URI, headers: Map<String, Any> = mapOf()): HttpResponse<String> {
        return request(uri, HttpRequestType.GET, headers)
    }

    fun put(uri: Any, headers: Map<String, Any> = mapOf(), send: Any? = null): HttpResponse<String> {
        return put(URI(uri.toString()), headers, send)
    }
    fun put(uri: URI, headers: Map<String, Any> = mapOf(), send: Any? = null): HttpResponse<String> {
        return request(uri, HttpRequestType.PUT, headers, send)
    }

    fun post(uri: Any, headers: Map<String, Any> = mapOf(), send: Any? = null): HttpResponse<String> {
        return post(URI(uri.toString()), headers, send)
    }
    fun post(uri: URI, headers: Map<String, Any> = mapOf(), send: Any? = null): HttpResponse<String> {
        return request(uri, HttpRequestType.POST, headers, send)
    }

    private fun request(
        uri: URI,
        type: HttpRequestType,
        headers: Map<String, Any> = mapOf(),
        send: Any? = null,
    ): HttpResponse<String> {

        val builder = HttpRequest.newBuilder(uri)
        headers.forEach { (string: String, obj: Any), -> builder.header(string, obj.toString()) }

        builder.method(type.name,
            if (send == null) HttpRequest.BodyPublishers.noBody()
            else HttpRequest.BodyPublishers.ofString(send.toString())
        )

        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    enum class HttpRequestType {
        GET, PUT, POST;
    }
}