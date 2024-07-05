package at.flauschigesalex.defaultLibrary.http;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public abstract class HttpHandler {

    public static HttpResponse<String> get(final @NotNull CharSequence uri) {
        return get(uri, null);
    }

    public static HttpResponse<String> get(final @NotNull CharSequence uri, final @Nullable Map<String, Object> headers) {
        return get(URI.create(uri.toString()), headers);
    }

    public static HttpResponse<String> get(final @NotNull URI uri) {
        return get(uri, null);
    }

    public static HttpResponse<String> get(final @NotNull URI uri, final @Nullable Map<String, Object> headers) {
        return request(uri, HttpRequestType.GET, headers, null);
    }

    public static HttpResponse<String> put(final @NotNull CharSequence uri, final @NotNull Object send) {
        return put(uri, null, send);
    }

    public static HttpResponse<String> put(final @NotNull CharSequence uri, final @Nullable Map<String, Object> headers, final @NotNull Object send) {
        return put(URI.create(uri.toString()), headers, send);
    }

    public static HttpResponse<String> put(final @NotNull URI uri, final @NotNull Object send) {
        return put(uri, null, send);
    }

    public static HttpResponse<String> put(final @NotNull URI uri, final @Nullable Map<String, Object> headers, final @NotNull Object send) {
        return request(uri, HttpRequestType.PUT, headers, send);
    }

    public static HttpResponse<String> post(final @NotNull CharSequence uri, final @NotNull Object send) {
        return post(uri, null, send);
    }

    public static HttpResponse<String> post(final @NotNull CharSequence uri, final @Nullable Map<String, Object> headers, final @NotNull Object send) {
        return post(URI.create(uri.toString()), headers, send);
    }

    public static HttpResponse<String> post(final @NotNull URI uri, final @NotNull Object send) {
        return post(uri, null, send);
    }

    public static HttpResponse<String> post(final @NotNull URI uri, final @Nullable Map<String, Object> headers, final @NotNull Object send) {
        return request(uri, HttpRequestType.POST, headers, send);
    }

    @SneakyThrows
    private static HttpResponse<String> request(final @NotNull URI uri, final @NotNull HttpRequestType type, @Nullable Map<String, Object> headers, final @Nullable Object send) {
        if (headers == null)
            headers = Map.of();

        final HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        headers.forEach((string, object) -> builder.header(string, object.toString()));
        builder.method(type.name(), send == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(send.toString()));

        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}