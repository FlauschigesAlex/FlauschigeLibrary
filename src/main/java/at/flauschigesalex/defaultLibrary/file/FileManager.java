package at.flauschigesalex.defaultLibrary.file;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;

@Getter
@SuppressWarnings({"UnusedReturnValue", "unused", "BooleanMethodIsAlwaysInverted"})
public final class FileManager {

    private final File file;
    private JsonManager jsonManager;

    public FileManager(final @NotNull String path) {
        this(new File(path));
    }
    public FileManager(final @NotNull File file) {
        this.file = file;
    }

    public boolean create(final @NotNull FileType fileType) {
        switch (fileType) {
            case FILE -> {
                return createFile();
            }
            case JSONFILE -> {
                return createJsonFile();
            }
            case DIRECTORY -> {
                return createDirectory();
            }
        }
        return false;
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

    public @Nullable String readString() {
        if (!isReadable())
            return null;

        final StringBuilder builder = new StringBuilder();
        int read;

        try {
            InputStream inputStream = new FileInputStream(file);
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

    public boolean write(final @NotNull String string) {
        if (!isWritable())
            return false;

        return write(string.getBytes());
    }

    public boolean write(final @NotNull JsonManager jsonManager) {
        return this.write(jsonManager.getContent());
    }

    public boolean write(final @NotNull StringBuilder builder) {
        return this.write(builder.toString());
    }

    public boolean write(final @NotNull InputStream inputStream) {
        final ArrayList<Byte> byteArray = new ArrayList<>();
        int read;
        try {
            while ((read = inputStream.read()) != -1)
                byteArray.add((byte) read);

            byte[] bytes = new byte[byteArray.size()];
            for (int bytePosition = 0; bytePosition < byteArray.size(); bytePosition++)
                bytes[bytePosition] = byteArray.get(bytePosition);

            this.write(bytes);
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

    public enum FileType {
        DIRECTORY, FILE, JSONFILE
    }

    public String toString() {
        return getFile().getPath() + "/";
    }
}
