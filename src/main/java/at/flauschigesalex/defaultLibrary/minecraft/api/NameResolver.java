package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.utils.Invisible;
import at.flauschigesalex.defaultLibrary.file.JsonManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("unused")
public final class NameResolver {

    private final String uuid;
    private @Invisible MojangAPI mojangAPI;

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

    public NameResolver instanced(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public String resolve() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        if (uuid == null)
            return null;
        for (String name : mojangAPI.cache.keySet()) {
            if (!mojangAPI.cache.get(name).equalsIgnoreCase(uuid)) continue;
            return name;
        }
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL("https://api.mojang.com/user/profile/" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            JsonManager jsonManager = JsonManager.parse(content.toString());
            if (jsonManager == null)
                return null;
            String name = jsonManager.asString("name");
            mojangAPI.cache.put(name, uuid);
            return name;
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    public String toString() {
        return resolve();
    }
}
