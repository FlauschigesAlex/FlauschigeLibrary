package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.file.JsonManager;
import at.flauschigesalex.defaultLibrary.http.HttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpResponse;
import java.util.UUID;

import static java.net.HttpURLConnection.HTTP_OK;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class UUIDResolver {

    private final String name;
    private MojangAPI mojangAPI;
    private boolean subString = false;

    UUIDResolver(final @Nullable String name) {
        this.name = name;
    }

    public UUIDResolver api(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public UUIDResolver substring() {
        this.subString = true;
        return this;
    }

    public @Nullable JsonManager getJsonManager() {
        if (mojangAPI == null)
            throw new NullPointerException("mojangAPI is not instanced!");

        if (name == null)
            return null;

        final HttpResponse<String> site = HttpHandler.get("https://api.mojang.com/users/profiles/minecraft/%s".formatted(name));
        if (site.statusCode() != HTTP_OK)
            return null;

        return JsonManager.of(site);
    }

    public String resolveString() {
        if (mojangAPI == null)
            throw new NullPointerException("mojangAPI is not instanced!");

        if (name == null)
            return null;

        for (final String name : mojangAPI.cache.keySet()) {
            if (!this.name.equalsIgnoreCase(name)) continue;

            final String uuid = mojangAPI.cache.get(name);
            final String subUuid = uuid.substring(0, 8) + "-"
                    + uuid.substring(8, 12) + "-"
                    + uuid.substring(12, 16) + "-"
                    + uuid.substring(16, 20) + "-"
                    + uuid.substring(20);

            return subString ? subUuid : uuid;
        }

        final JsonManager siteJson = getJsonManager();
        if (siteJson == null)
            return null;

        final String name = siteJson.asString("name");
        final String uuid = siteJson.asString("id");
        mojangAPI.cache.put(name, uuid);

        if (uuid == null)
            return null;

        final String uuidSubString = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
        return subString ? uuidSubString : uuid;
    }

    public UUID resolve() {
        this.substring();

        if (mojangAPI == null)
            throw new NullPointerException("mojangAPI is not instanced!");

        final String uuid = this.resolveString();
        if (uuid == null)
            return null;

        try {
            return UUID.fromString(uuid);
        } catch (Exception ignore) {}
        return null;
    }

    @Override
    public String toString() {
        return resolveString();
    }
}
