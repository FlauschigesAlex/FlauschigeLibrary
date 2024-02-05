package at.flauschigesalex.defaultLibrary.http;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.net.HttpURLConnection;
import java.net.URL;

@Getter
public final class HttpConnection {

    public static HttpConnection from(final @NotNull String url) {

    }

    public static HttpConnection from(final @NotNull URL url) {

    }

    private final URL url;
    private HttpConnection(final @NotNull URL url) {
        this.url = url;
    }

    public HttpResponse openConnection() {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        } catch (Exception ignore) {
        }
    }
}
