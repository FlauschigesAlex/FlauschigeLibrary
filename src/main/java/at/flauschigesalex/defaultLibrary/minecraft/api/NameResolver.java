package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.file.JsonManager;
import at.flauschigesalex.defaultLibrary.http.HttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("unused")
public final class NameResolver {

    private final String uuid;
    private MojangAPI mojangAPI;

    NameResolver(final @Nullable UUID uuid) {
        if (uuid == null) {
            this.uuid = null;
            return;
        }
        this.uuid = uuid.toString().replace("-", "");
    }

    NameResolver(final @Nullable String uuid) {
        if (uuid == null) {
            this.uuid = null;
            return;
        }
        this.uuid = uuid.replace("-", "");
    }

    public NameResolver api(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public @Nullable JsonManager getJsonManager() {
        if (mojangAPI == null)
            throw new NullPointerException("mojangAPI is not instanced!");

        if (uuid == null)
            return null;

        final HttpHandler site = HttpHandler.get("https://api.mojang.com/user/profile/" + uuid);
        if (site.getResponseCode() != 200)
            return null;

        return JsonManager.parse(site.getSiteBody());
    }

    public String resolve() {
        for (String name : mojangAPI.cache.keySet())
            if (mojangAPI.cache.get(name).equalsIgnoreCase(uuid))
                return name;

        final JsonManager siteJson = getJsonManager();
        if (siteJson == null)
            return null;

        final String uuid = siteJson.asString("id");
        final String name = siteJson.asString("name");
        mojangAPI.cache.put(name, uuid);

        return name;
    }

    @Override
    public String toString() {
        return resolve();
    }
}
