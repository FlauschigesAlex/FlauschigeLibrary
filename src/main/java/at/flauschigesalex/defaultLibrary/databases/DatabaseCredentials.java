package at.flauschigesalex.defaultLibrary.databases;

import at.flauschigesalex.defaultLibrary.file.FileHandler;
import at.flauschigesalex.defaultLibrary.file.JsonManager;
import at.flauschigesalex.defaultLibrary.file.ResourceHandler;
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
public final class DatabaseCredentials {

    private static final int defaultPort = 27017;
    private final ArrayList<String> hostnames;
    private final String username;
    private final String password;
    private final String database;
    private final ArrayList<Integer> ports;

    DatabaseCredentials(final @Nullable ArrayList<String> hostnames, final @Nullable String username, final @Nullable String password, final @Nullable String database, final @Nullable ArrayList<Integer> ports) {
        if (hostnames == null || hostnames.isEmpty())
            throw new DatabaseLoginException("hostnames is null or empty");
        if (username == null)
            throw new DatabaseLoginException("username is null");
        if (password == null)
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
        this.password = password;
        this.database = database;
        this.ports = ports;
    }

    public static DatabaseCredentials construct(final @NotNull String hostname, final @NotNull String username, final @NotNull CharSequence password, final @NotNull String database) {
        return new DatabaseCredentials(new ArrayList<>(List.of(hostname)), username, password.toString(), database, new ArrayList<>(List.of(defaultPort)));
    }

    public static DatabaseCredentials construct(final @NotNull String hostname, final @NotNull String username, final @NotNull CharSequence password, final @NotNull String database, final int port) {
        return new DatabaseCredentials(new ArrayList<>(List.of(hostname)), username, password.toString(), database, new ArrayList<>(List.of(port)));
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

    public static DatabaseCredentials construct(final @NotNull FileHandler fileHandler) {
        if (fileHandler.readString() == null)
            throw new DatabaseLoginException("Failed to read fileInputStream for file " + fileHandler.getFile().getName() + " at:" + fileHandler.getClass().getSimpleName() + "\n" + fileHandler.getFile().getPath());
        return construct(fileHandler.readString());
    }

    public static DatabaseCredentials construct(final @NotNull ResourceHandler resourceHandler) {
        return construct(resourceHandler.getUrl());
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
        final JsonManager jsonManager = new JsonManager(jsonString);
        final String[] requiredCredentials = new String[]{"hostname", "username", "accessKey", "database"};

        String username = null;
        final Object userObject = jsonManager.getObject("username");
        if (userObject instanceof String string)
            username = string;

        String accessKey = null;
        final Object accessObject = jsonManager.getObject("accessKey");
        if (accessObject instanceof String string)
            accessKey = string;

        String database = null;
        final Object databaseObject = jsonManager.getObject("database");
        if (databaseObject instanceof String string)
            database = string;


        final ArrayList<Integer> ports = new ArrayList<>();
        if (jsonManager.has("port")) {
            final Object portObject = jsonManager.getObject("port");
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
        if (jsonManager.has("hostname")) {
            final Object hostObject = jsonManager.getObject("hostname");
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
