package at.flauschigesalex.defaultLibrary.http;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.net.HttpURLConnection;

@Getter
public final class HttpResponse {

    private final HttpURLConnection urlConnection;
    HttpResponse(final HttpURLConnection urlConnection) {
        this.urlConnection = urlConnection;
    }

    public @Nullable Integer getResponseCode() {
        try {
            return getUrlConnection().getResponseCode();
        } catch (Exception ignore) {
        }
        return null;
    }

    public @Nullable String getResponseMessage() {
        try {
            return getUrlConnection().getResponseMessage();
        } catch (Exception ignore) {
        }
        return null;
    }

    public @Nullable InputStream getInputStream() {
        try {
            return getUrlConnection().getInputStream();
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean isOK() {
        return getResponseCode() != null && getResponseCode() == 200;
    }
}
