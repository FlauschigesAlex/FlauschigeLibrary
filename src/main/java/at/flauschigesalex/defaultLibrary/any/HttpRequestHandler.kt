package at.flauschigesalex.defaultLibrary.any

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers

@Suppress("unused", "MemberVisibilityCanBePrivate")
object HttpRequestHandler {

    @Suppress("UNCHECKED_CAST")
    fun <A: Any> request(url: Any,
                method: HttpMethod = HttpMethod.GET,
                handler: BodyHandler<A> = BodyHandlers.ofString() as BodyHandler<A>,
                headers: Map<String, Any> = mapOf(),
                data: Any? = null
    ): HttpResponse<A> {
        val builder = HttpRequest.newBuilder(URI.create(url.toString()))
        headers.forEach { (string: String, obj: Any), -> builder.header(string, obj.toString()) }

        builder.method(method.name,
            if (data == null) HttpRequest.BodyPublishers.noBody()
            else HttpRequest.BodyPublishers.ofString(data.toString())
        )

        return HttpClient.newHttpClient().send(builder.build(), handler)
    }
}

enum class HttpMethod {
  GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE, CONNECT;
}