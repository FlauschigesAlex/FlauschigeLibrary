package at.flauschigesalex.defaultLibrary.utils.file;

import at.flauschigesalex.defaultLibrary.FlauschigeLibrary;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Getter
@SuppressWarnings("unused")
public final class ResourceManager {

    public static @Nullable ResourceManager getResource(String sourcePath) {
        URL url = FlauschigeLibrary.getLibrary().getClass().getClassLoader().getResource(sourcePath);
        if (url == null) return null;
        return new ResourceManager(url);
    }
    private final URL url;
    @Getter(AccessLevel.NONE)
    private JsonManager jsonManager;

    ResourceManager(@NotNull URL url) {
        this.url = url;
    }

    public @Nullable String read() {
        if (!isReadable())
            return null;
        StringBuilder builder = new StringBuilder();
        int read;
        try {
            InputStream inputStream = url.openStream();
            while ((read = inputStream.read()) != -1) {
                builder.append((char) read);
            }
            inputStream.close();
            return builder.toString();
        } catch (Exception fail) {
            fail.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@ {"
                + "\nurl:" + url
                + "\nread:" + read()
                + "\n}";
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
            InputStream stream = url.openStream();
            if (stream == null)
                return false;
            stream.close();
            return true;
        } catch (IOException fail) {
            fail.printStackTrace();
        }
        return false;
    }
}
