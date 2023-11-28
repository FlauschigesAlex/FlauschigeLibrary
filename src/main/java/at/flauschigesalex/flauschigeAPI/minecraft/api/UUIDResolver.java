package at.flauschigesalex.flauschigeAPI.minecraft.api;

import at.flauschigesalex.flauschigeAPI.utils.file.JsonManager;
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

    UUIDResolver(String name) {
        this.name = name;
    }

    public UUIDResolver instanced(MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public UUIDResolver subString() {
        this.subString = true;
        return this;
    }

    @SuppressWarnings("deprecation")
    public String resolveString() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        for (String name : mojangAPI.cache.keySet()) {
            if (!this.name.equalsIgnoreCase(name)) continue;
            final String uuid = mojangAPI.cache.get(name);
            String uuidSubString = uuid.substring(0,8)+"-"+uuid.substring(8,12)+"-"+uuid.substring(12,16)+"-"+uuid.substring(16,20)+"-"+uuid.substring(20);
            if (subString) return uuidSubString;
            return uuid;
        }
        try {
            StringBuilder content = new StringBuilder();
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
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
            String uuid = jsonManager.asString("id");
            if (uuid == null) return null;
            String uuidSubString = uuid.substring(0,8)+"-"+uuid.substring(8,12)+"-"+uuid.substring(12,16)+"-"+uuid.substring(16,20)+"-"+uuid.substring(20);
            mojangAPI.cache.put(name, uuid);
            return subString?uuidSubString:uuid;
        } catch (Exception ignore) {}
        return null;
    }

    public UUID resolve() throws NullPointerException {
        this.subString();
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        String uuid = this.resolveString();
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
