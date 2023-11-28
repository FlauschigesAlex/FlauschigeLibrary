package at.flauschigesalex.flauschigeAPI.database;

import at.flauschigesalex.flauschigeAPI.utils.file.FileManager;
import at.flauschigesalex.flauschigeAPI.utils.file.JsonManager;
import at.flauschigesalex.flauschigeAPI.utils.file.ResourceManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.net.URL;

@Getter
@SuppressWarnings({"unused", "DataFlowIssue"})
public class DatabaseCredentials {

    public static DatabaseCredentials construct(@NotNull String hostname, @NotNull String username, @NotNull CharSequence accessKey, @NotNull String database) {
        return new DatabaseCredentials(new String[]{hostname, username, accessKey.toString(), database}, defaultPort);
    }
    public static DatabaseCredentials construct(@NotNull String hostname, @NotNull String username, @NotNull CharSequence accessKey, @NotNull String database, short port) {
        return new DatabaseCredentials(new String[]{hostname, username, accessKey.toString(), database}, port);
    }
    public static DatabaseCredentials construct(@NotNull File file) {

        if (!file.exists())
            throw new DatabaseLoginException("Failed to find file "+file.getName()+" at:\n"+file.getAbsolutePath());
        if (!file.isFile())
            throw new DatabaseLoginException("Failed to access file "+file.getName()+" at:\n"+file.getAbsolutePath());
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return construct(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new DatabaseLoginException("Failed to read fileInputStream for file "+file.getName()+" at:"+file.getAbsolutePath()+"\n"+e.getMessage());
        }
    }
    public static DatabaseCredentials construct(@NotNull FileManager fileManager) {
        if (fileManager.read() == null)
            throw new DatabaseLoginException("Failed to read fileInputStream for file "+fileManager.getFile().getName()+" at:"+fileManager.getClass().getSimpleName()+"\n"+fileManager.getFile().getPath());
        return construct(fileManager.read());
    }
    public static DatabaseCredentials construct(@NotNull ResourceManager resourceManager) {
        return construct(resourceManager.getUrl());
    }
    public static DatabaseCredentials construct(@NotNull URL resource) {
        try {
            return construct(resource.openStream());
        } catch (IOException e) {
            throw new DatabaseLoginException("Failed to read fileInputStream for resource "+resource.getFile()+" at:"+resource.getPath()+"\n"+e.getMessage());
        }
    }
    public static DatabaseCredentials construct(@NotNull InputStream fileInputStream) {
        int read;
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                if ((read = fileInputStream.read()) == -1) break;
            } catch (IOException e) {
                throw new DatabaseLoginException("Failed to read fileInputStream:\n"+e.getMessage());
            }
            builder.append((char) read);
        }
        return construct(builder);
    }
    public static DatabaseCredentials construct(@NotNull StringBuilder jsonStringBuilder) {
        return construct(jsonStringBuilder.toString());
    }
    public static DatabaseCredentials construct(@NotNull String jsonString) {
        JsonManager jsonManager = JsonManager.parse(jsonString);
        String[] requiredCredentials = new String[]{"hostname","username","accessKey","database"};
        String[] credentials = new String[requiredCredentials.length+1];
        for (int loginCredential = 0; loginCredential < requiredCredentials.length; loginCredential++) {
            if (!jsonManager.contains(requiredCredentials[loginCredential]))
                return null;
            credentials[loginCredential] = jsonManager.asString(requiredCredentials[loginCredential]);
        }
        short port = defaultPort;
        if (jsonManager.contains("port")) {
            Object portObject = jsonManager.asObject("port");
            if (!(portObject instanceof Short portShort))
                throw new DatabaseLoginException("Required credential \""+portObject+"\" is not an instance of \"Short\".");
            port = portShort;
        }
        return new DatabaseCredentials(credentials, port);
    }

    private final String hostname;
    private final String username;
    private final String accessKey;
    private final String database;
    private final short port;
    private static final short defaultPort = 27017;

    DatabaseCredentials(String[] credentials, short port) {
        this.hostname = credentials[0];
        this.username = credentials[1];
        this.accessKey = credentials[2];
        this.database = credentials[3];
        this.port = port;
    }
}
