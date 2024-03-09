package at.flauschigesalex.defaultLibrary.http;

import at.flauschigesalex.defaultLibrary.fileUtils.JsonManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.URI;
import java.net.http.*;
import java.util.*;

@SuppressWarnings("unused")
@Getter
public final class HttpHandler {

    public static HttpHandler get(final @NotNull CharSequence uri) {
        return get(URI.create(uri.toString()));
    }
    public static HttpHandler get(final @NotNull URI uri) {
        return get(uri, new HashMap<>());
    }
    public static HttpHandler get(final @NotNull CharSequence uri, final @NotNull Map<String, Object> header) {
        return get(URI.create(uri.toString()), header);
    }
    public static HttpHandler get(final @NotNull URI uri, final @NotNull Map<String, Object> header) {
        return new HttpHandler(uri, HttpRequestType.GET, JsonManager.createNew(), header);
    }

    public static HttpHandler post(final @NotNull CharSequence uri, final @NotNull JsonManager jsonManager) {
        return post(URI.create(uri.toString()), new HashMap<>(), jsonManager);
    }
    public static HttpHandler post(final @NotNull URI uri, final @NotNull JsonManager jsonManager) {
        return new HttpHandler(uri, HttpRequestType.POST, jsonManager, new HashMap<>());
    }
    public static HttpHandler post(final @NotNull CharSequence uri, final @NotNull Map<String, Object> header, final @NotNull JsonManager jsonManager) {
        return post(URI.create(uri.toString()), header, jsonManager);
    }
    public static HttpHandler post(final @NotNull URI uri, final @NotNull Map<String, Object> header, final @NotNull JsonManager jsonManager) {
        return new HttpHandler(uri, HttpRequestType.POST, jsonManager, header);
    }

    public static HttpHandler put(final @NotNull CharSequence uri, final @NotNull JsonManager jsonManager) {
        return put(URI.create(uri.toString()), new HashMap<>(), jsonManager);
    }
    public static HttpHandler put(final @NotNull URI uri, final @NotNull JsonManager jsonManager) {
        return new HttpHandler(uri, HttpRequestType.PUT, jsonManager, new HashMap<>());
    }
    public static HttpHandler put(final @NotNull CharSequence uri, final @NotNull Map<String, Object> header, final @NotNull JsonManager jsonManager) {
        return put(URI.create(uri.toString()), header, jsonManager);
    }
    public static HttpHandler put(final @NotNull URI uri, final @NotNull Map<String, Object> header, final @NotNull JsonManager jsonManager) {
        return new HttpHandler(uri, HttpRequestType.PUT, jsonManager, header);
    }

    private int responseCode = 0;
    private String siteBody = "";
    private @Nullable HttpHeaders siteHeaders = null;
    private @Nullable HttpRequest siteRequest = null;

    private HttpHandler(final @NotNull URI uri, final @NotNull HttpRequestType type, final @NotNull JsonManager post, final @NotNull Map<String, Object> set) {

        try {
            switch (type) {
                case GET -> {
                    final HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET();
                    set.forEach((key, value) -> {
                        if (key != null && value != null)
                            builder.setHeader(key, value.toString());
                    });
                    final HttpResponse<String> response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
                    this.responseCode = response.statusCode();
                    this.siteBody = response.body();
                    this.siteHeaders = response.headers();
                    this.siteRequest = response.request();
                }

                case POST -> {
                    final HttpRequest.Builder builder = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(post.asJsonString()));
                    set.forEach((key, value) -> {
                        if (key != null && value != null)
                            builder.header(key, value.toString());
                    });
                    final HttpResponse<String> response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
                    this.responseCode = response.statusCode();
                    this.siteBody = response.body();
                    this.siteHeaders = response.headers();
                    this.siteRequest = response.request();
                }

                case PUT -> {
                    final HttpRequest.Builder builder = HttpRequest.newBuilder(uri).PUT(HttpRequest.BodyPublishers.ofString(post.asJsonString()));
                    set.forEach((key, value) -> {
                        if (key != null && value != null)
                            builder.header(key, value.toString());
                    });
                    final HttpResponse<String> response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
                    this.responseCode = response.statusCode();
                    this.siteBody = response.body();
                    this.siteHeaders = response.headers();
                    this.siteRequest = response.request();
                }
            }
        } catch (final Exception fail) {
            fail.printStackTrace();
        }
    }
}
