package at.flauschigesalex.defaultLibrary.file;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Getter
@SuppressWarnings({"unused", "resource"})
public final class ResourceHandler {

    private final URL url;
    @Getter(AccessLevel.NONE)
    private JsonManager jsonManager;

    public ResourceHandler(final @NotNull String string) {
        final URL url = FlauschigeLibrary.getLibrary().getClass().getClassLoader().getResource(string);

        if (url == null)
            throw new NullPointerException("URL cannot be null!");

        this.url = url;
    }

    public ResourceHandler(final @NotNull URL url) {
        this.url = url;
    }

    public @Nullable InputStream readStream() {
        try {
            return url.openStream();
        } catch (final Exception ignore) {
        }
        return null;
    }

    public @Nullable String readString() {
        if (!isReadable())
            return null;

        final StringBuilder builder = new StringBuilder();
        final InputStream stream = readStream();
        if (stream != null) {
            try {
                for (byte nom : stream.readAllBytes())
                    builder.append((char) nom);
                return builder.toString();
            } catch (final IOException ignore) {
            }
        }
        return null;
    }

    public boolean isReadable() {
        return readStream() != null;
    }
}
