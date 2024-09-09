package at.flauschigesalex.defaultLibrary.file;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

@Getter
@SuppressWarnings({"UnusedReturnValue", "unused", "BooleanMethodIsAlwaysInverted"})
public final class FileHandler {

    private final File file;
    private JsonManager jsonManager;

    public FileHandler(final @NotNull String path) {
        this(new File(path));
    }
    public FileHandler(final @NotNull File file) {
        this.file = file;
    }

    public boolean createFile() {
        if (file.exists())
            return true;

        try {
            return file.createNewFile();
        } catch (final IOException fail) {
            fail.printStackTrace();
        }
        return false;
    }

    public boolean createJsonFile() {
        if (file.exists())
            return true;

        return createFile() && write("{}");
    }

    public boolean createDirectory() {
        if (file.exists())
            return true;

        return file.mkdir();
    }

    public boolean delete() {
        return purge(file);
    }

    public @Nullable InputStream readStream() {
        try {
            return new FileInputStream(file);
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

    public boolean write(final @NotNull Object object) {
        return this.write(object.toString().getBytes());
    }

    public boolean write(final @NotNull InputStream inputStream) {
        try {
            this.write(inputStream.readAllBytes());
        } catch (Exception ignore) {}
        return false;
    }

    public boolean write(final byte[] bytes) {
        if (!isWritable())
            return false;

        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
            stream.close();
            return true;
        } catch (Exception fail) {
            fail.printStackTrace();
        }
        return false;
    }

    private boolean purge(final @NotNull File file) {
        if (!file.exists())
            return true;

        if (file.isDirectory()) {
            final File[] contents = file.listFiles();
            if (contents != null)
                for (File contentFile : contents)
                    if (!purge(contentFile))
                        return false;
        }
        return file.delete();
    }

    public boolean isReadable() {
        return file.exists() && file.isFile() && file.canRead();
    }

    public boolean isWritable() {
        return this.isReadable() && file.canWrite();
    }

    public String toString() {
        return getFile().getPath() + "/";
    }
}
