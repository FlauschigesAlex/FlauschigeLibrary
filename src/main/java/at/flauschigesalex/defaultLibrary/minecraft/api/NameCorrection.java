package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.utils.Invisible;
import at.flauschigesalex.defaultLibrary.file.JsonManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
public final class NameCorrection {

    private final String name;
    private @Invisible MojangAPI mojangAPI;

    NameCorrection(final @Nullable String name) {
        this.name = name;
    }

    public NameCorrection instanced(final @NotNull MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public String correct() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        if (name == null)
            return null;
        for (String name : mojangAPI.cache.keySet()) {
            if (!this.name.equalsIgnoreCase(name)) continue;
            return name;
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
            JsonManager jsonManager = JsonManager.parse(content.toString());
            if (jsonManager == null) return null;
            final String name = jsonManager.asString("name");
            final String uuid = jsonManager.asString("id");
            mojangAPI.cache.put(name, uuid);
            return name;
        } catch (final Exception ignore) {
        }
        return null;
    }

    @Override
    public String toString() {
        return correct();
    }
}
