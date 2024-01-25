package at.flauschigesalex.defaultLibrary.fileUtils;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import at.flauschigesalex.defaultLibrary.utils.AutoDisplayable;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Getter
@SuppressWarnings("unused")
public final class ResourceManager extends AutoDisplayable {

    private final URL url;
    @Getter(AccessLevel.NONE)
    private JsonManager jsonManager;
    ResourceManager(final @NotNull URL url) {
        this.url = url;
    }

    public static @Nullable ResourceManager getResource(final @NotNull String sourcePath) {
        final URL url = FlauschigeLibrary.getLibrary().getClass().getClassLoader().getResource(sourcePath);
        if (url == null) return null;
        return new ResourceManager(url);
    }

    public @Nullable String read() {
        if (!isReadable())
            return null;
        final StringBuilder builder = new StringBuilder();
        int read;
        try {
            final InputStream inputStream = url.openStream();
            while ((read = inputStream.read()) != -1) {
                builder.append((char) read);
            }
            inputStream.close();
            return builder.toString();
        } catch (final Exception fail) {
            fail.printStackTrace();
        }
        return null;
    }

    public @Nullable JsonManager getJsonManager() {
        final String read = read();
        if (read == null)
            return null;
        if (jsonManager == null)
            jsonManager = JsonManager.parse(read);
        return jsonManager;
    }

    public boolean isReadable() {
        try {
            final InputStream stream = url.openStream();
            if (stream == null)
                return false;
            stream.close();
            return true;
        } catch (final IOException fail) {
            fail.printStackTrace();
        }
        return false;
    }
}
