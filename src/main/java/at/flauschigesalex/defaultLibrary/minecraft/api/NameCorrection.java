package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.utils.file.JsonManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("unused")
public final class NameCorrection {
    private final String name;
    private MojangAPI mojangAPI;

    NameCorrection(String name) {
        this.name = name;
    }

    public NameCorrection instanced(MojangAPI mojangAPI) {
        this.mojangAPI = mojangAPI;
        return this;
    }

    public String correct() throws NullPointerException {
        if (mojangAPI == null) {
            throw new NullPointerException("mojangAPI is not instanced!");
        }
        for (String name : mojangAPI.cache.keySet()) {
            if (!this.name.equalsIgnoreCase(name)) continue;
            return name;
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
            mojangAPI.cache.put(name, uuid);
            return name;
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    public String toString() {
        return correct();
    }
}
