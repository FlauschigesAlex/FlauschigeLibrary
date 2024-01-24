package at.flauschigesalex.defaultLibrary.database;

import at.flauschigesalex.defaultLibrary.utils.Invisible;
import at.flauschigesalex.defaultLibrary.utils.Printable;
import at.flauschigesalex.defaultLibrary.utils.file.FileManager;
import at.flauschigesalex.defaultLibrary.utils.file.JsonManager;
import at.flauschigesalex.defaultLibrary.utils.file.ResourceManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings({"unused", "DataFlowIssue"})
public final class DatabaseCredentials extends Printable {

    private static final int defaultPort = 27017;
    private final ArrayList<String> hostnames;
    private final String username;
    private final @Invisible String accessKey;
    private final String database;
    private final ArrayList<Integer> ports;

    DatabaseCredentials(final @Nullable ArrayList<String> hostnames, final @Nullable String username, final @Nullable String accessKey, final @Nullable String database, final @Nullable ArrayList<Integer> ports) {
        if (hostnames == null || hostnames.isEmpty())
            throw new DatabaseLoginException("hostnames is null or empty");
        if (username == null)
            throw new DatabaseLoginException("username is null");
        if (accessKey == null)
            throw new DatabaseLoginException("accessKey is null");
        if (database == null)
            throw new DatabaseLoginException("database is null");
        if (ports == null || ports.isEmpty())
            throw new DatabaseLoginException("ports is null or empty");
        while (ports.size() < hostnames.size()) {
            ports.add(defaultPort);
        }
        this.hostnames = hostnames;
        this.username = username;
        this.accessKey = accessKey;
        this.database = database;
        this.ports = ports;
    }

    public static DatabaseCredentials construct(final @NotNull String hostname, final @NotNull String username, final @NotNull CharSequence accessKey, final @NotNull String database) {
        return new DatabaseCredentials(new ArrayList<>(List.of(hostname)), username, accessKey.toString(), database, new ArrayList<>(List.of(defaultPort)));
    }

    public static DatabaseCredentials construct(final @NotNull String hostname, final @NotNull String username, final @NotNull CharSequence accessKey, final @NotNull String database, final int port) {
        return new DatabaseCredentials(new ArrayList<>(List.of(hostname)), username, accessKey.toString(), database, new ArrayList<>(List.of(port)));
    }

    public static DatabaseCredentials construct(final @NotNull File file) {

        if (!file.exists())
            throw new DatabaseLoginException("Failed to find file " + file.getName() + " at:\n" + file.getAbsolutePath());
        if (!file.isFile())
            throw new DatabaseLoginException("Failed to access file " + file.getName() + " at:\n" + file.getAbsolutePath());
        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            return construct(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new DatabaseLoginException("Failed to read fileInputStream for file " + file.getName() + " at:" + file.getAbsolutePath() + "\n" + e.getMessage());
        }
    }

    public static DatabaseCredentials construct(final @NotNull FileManager fileManager) {
        if (fileManager.read() == null)
            throw new DatabaseLoginException("Failed to read fileInputStream for file " + fileManager.getFile().getName() + " at:" + fileManager.getClass().getSimpleName() + "\n" + fileManager.getFile().getPath());
        return construct(fileManager.read());
    }

    public static DatabaseCredentials construct(final @NotNull ResourceManager resourceManager) {
        return construct(resourceManager.getUrl());
    }

    public static DatabaseCredentials construct(final @NotNull URL resource) {
        try {
            return construct(resource.openStream());
        } catch (IOException e) {
            throw new DatabaseLoginException("Failed to read fileInputStream for resource " + resource.getFile() + " at:" + resource.getPath() + "\n" + e.getMessage());
        }
    }

    public static DatabaseCredentials construct(final @NotNull InputStream fileInputStream) {
        int read;
        final StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                if ((read = fileInputStream.read()) == -1) break;
            } catch (IOException e) {
                throw new DatabaseLoginException("Failed to read fileInputStream:\n" + e.getMessage());
            }
            builder.append((char) read);
        }
        return construct(builder);
    }

    public static DatabaseCredentials construct(final @NotNull StringBuilder jsonStringBuilder) {
        return construct(jsonStringBuilder.toString());
    }

    public static DatabaseCredentials construct(final @NotNull String jsonString) {
        final JsonManager jsonManager = JsonManager.parse(jsonString);
        if (jsonManager == null)
            throw new DatabaseLoginException("Failed to create " + DatabaseCredentials.class.getSimpleName() + " from string: " + jsonString);
        final String[] requiredCredentials = new String[]{"hostname", "username", "accessKey", "database"};

        String username = null;
        final Object userObject = jsonManager.asObject("username");
        if (userObject instanceof String string)
            username = string;

        String accessKey = null;
        final Object accessObject = jsonManager.asObject("accessKey");
        if (accessObject instanceof String string)
            accessKey = string;

        String database = null;
        final Object databaseObject = jsonManager.asObject("database");
        if (databaseObject instanceof String string)
            database = string;


        final ArrayList<Integer> ports = new ArrayList<>();
        if (jsonManager.contains("port")) {
            final Object portObject = jsonManager.asObject("port");
            if (portObject instanceof Integer portShort)
                ports.add(portShort);
            if (portObject instanceof JSONArray jsonArray)
                for (final Object o : jsonArray) {
                    if (o == null) continue;
                    try {
                        ports.add(Integer.parseInt(o.toString()));
                    } catch (Exception ignore) {
                    }
                }
        }

        final ArrayList<String> hosts = new ArrayList<>();
        if (jsonManager.contains("hostname")) {
            final Object hostObject = jsonManager.asObject("hostname");
            if (hostObject instanceof String hostString)
                hosts.add(hostString);
            if (hostObject instanceof JSONArray jsonArray)
                for (final Object o : jsonArray) {
                    if (o instanceof String hostString) {
                        hosts.add(hostString);
                    }
                }
        }

        if (ports.isEmpty())
            ports.add(defaultPort);

        return new DatabaseCredentials(hosts, username, accessKey, database, ports);
    }
}
