package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.file.JsonManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class UUIDResolver {

    private final String name;
    private MojangAPI mojangAPI;
    private boolean subString = false;

    UUIDResolver(final @Nullable String name) {
        this.name = name;
    }

    public UUIDResolver instanced(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public UUIDResolver subString() {
        this.subString = true;
        return this;
    }

    public String resolveString() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        if (name == null)
            return null;
        for (final String name : mojangAPI.cache.keySet()) {
            if (!this.name.equalsIgnoreCase(name)) continue;
            final String uuid = mojangAPI.cache.get(name);
            final String uuidSubString = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
            if (subString) return uuidSubString;
            return uuid;
        }
        try {
            final StringBuilder content = new StringBuilder();
            final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            final JsonManager jsonManager = JsonManager.parse(content.toString());
            if (jsonManager == null)
                return null;

            final String name = jsonManager.asString("name");
            final String uuid = jsonManager.asString("id");
            if (uuid == null) return null;
            final String uuidSubString = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
            mojangAPI.cache.put(name, uuid);
            return subString ? uuidSubString : uuid;
        } catch (final Exception ignore) {
        }
        return null;
    }

    public UUID resolve() throws NullPointerException {
        this.subString();
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        final String uuid = this.resolveString();
        if (uuid == null)
            return null;

        try {
            return UUID.fromString(uuid);
        } catch (Exception fail) {
            return null;
        }
    }

    @Override
    public String toString() {
        return resolveString();
    }
}
