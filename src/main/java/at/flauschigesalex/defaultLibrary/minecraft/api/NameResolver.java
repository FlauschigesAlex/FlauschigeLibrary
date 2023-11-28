package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.utils.file.JsonManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("unused")
public final class NameResolver {
    private final String uuid;
    private MojangAPI mojangAPI;

    NameResolver(UUID uuid) {
        this.uuid = uuid.toString().replace("-", "");
    }

    NameResolver(String uuid) {
        this.uuid = uuid.replace("-", "");
    }

    public NameResolver instanced(MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    @SuppressWarnings("deprecation")
    public String resolve() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
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
