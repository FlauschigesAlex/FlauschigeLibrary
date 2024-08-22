package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.file.JsonManager;
import at.flauschigesalex.defaultLibrary.http.HttpHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpResponse;

import static java.net.HttpURLConnection.HTTP_OK;

@SuppressWarnings("unused")
public final class NameCorrection {

    private final String name;
    private MojangAPI mojangAPI;

    NameCorrection(final @Nullable String name) {
        this.name = name;
    }

    public NameCorrection instanced(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
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

        return new JsonManager(site);
    }

    public String correct() throws NullPointerException {
        if (mojangAPI == null)
            throw new NullPointerException("mojangAPI is not instanced!");

        if (name == null)
            return null;

        for (final String name : mojangAPI.cache.keySet())
            if (this.name.equalsIgnoreCase(name))
                return name;

        final JsonManager siteJson = getJsonManager();
        if (siteJson == null)
            return null;

        final String name = siteJson.asString("name");
        final String uuid = siteJson.asString("id");
        mojangAPI.cache.put(name, uuid);

        return name;
    }

    @Override
    public String toString() {
        return correct();
    }
}
