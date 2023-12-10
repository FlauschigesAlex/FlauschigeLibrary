package at.flauschigesalex.defaultLibrary.utils.file;

import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.util.ArrayList;

@Getter
@MongoIgnore
@SuppressWarnings({"UnusedReturnValue", "unused", "BooleanMethodIsAlwaysInverted"})
public final class FileManager {

    public static FileManager getFile(@NotNull File file) {
        return new FileManager(file);
    }
    public static FileManager getFile(@NotNull String path) {
        return new FileManager(new File(path));
    }

    private final File file;

    FileManager(File file) {
        this.file = file;
    }

    public boolean create(@NotNull FileType fileType) {
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
        } catch (IOException fail) {
            fail.printStackTrace();
            return false;
        }
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

    private boolean purge(final @NotNull File file) {
        if (!file.exists())
            return true;
        if (file.isDirectory()) {
            final File[] contents = file.listFiles();
            if (contents != null) {
                for (File contentFile : contents) {
                    if (!purge(contentFile))
                        return false;
                }
            }
        }
        return file.delete();
    }

    @Getter(AccessLevel.NONE) private JsonManager jsonManager;
    public @Nullable JsonManager jsonFile() {
        final String read = read();
        if (read == null)
            return null;
        if (jsonManager == null)
            jsonManager = JsonManager.parse(read).file(this);
        return jsonManager;
    }

    public @Nullable String read() {
        if (!isReadable())
            return null;
        StringBuilder builder = new StringBuilder(); int read;
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

    public boolean write(@NotNull String string) {
        if (!isWritable())
            return false;
        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(string.getBytes());
            stream.close();
            return true;
        } catch (Exception fail) {
            fail.printStackTrace();
        }
        return false;
    }
    public boolean write(@NotNull JsonManager jsonManager) {
        return this.write(jsonManager.getSource());
    }
    public boolean write(@NotNull StringBuilder builder) {
        return this.write(builder.toString());
    }
    public boolean write(@NotNull InputStream inputStream) {
        StringBuilder builder = new StringBuilder(); int read;
        try {
            while ((read = inputStream.read()) != -1) {
                builder.append((char) read);
            }
            return write(builder);
        } catch (Exception fail) {
            fail.printStackTrace();
        }
        return false;
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

    @Override
    public String toString() {
        return getClass().getSimpleName()+"@ {"
                +"\nfile: "+getFile().getName()
                +"\nreadable: "+isReadable()
                +"\nwritable: "+isWritable()
                +"\ncontent: "+read()
                +"\n}";
    }
}
