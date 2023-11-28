package at.flauschigesalex.flauschigeAPI.minecraft.api;

import javax.annotation.CheckReturnValue;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings({"unused", "ConstantValue"})
public final class MojangAPI {

    private static MojangAPI mojangAPI;
    public static MojangAPI mojangAPI() {
        if (mojangAPI == null) mojangAPI = new MojangAPI();
        return mojangAPI;
    }
    HashMap<String, String> cache = new HashMap<>();

    private MojangAPI() {}

    @CheckReturnValue
    public NameResolver nameResolver(UUID uuid) {
        return new NameResolver(uuid).instanced(this);
    }
    @CheckReturnValue
    public NameResolver nameResolver(String uuid) {
        return new NameResolver(uuid).instanced(this);
    }

    @CheckReturnValue
    public UUIDResolver uuidResolver(String name) {
        return new UUIDResolver(name).instanced(this);
    }

    @CheckReturnValue
    public NameCorrection nameCorrection(String name) {
        return new NameCorrection(name).instanced(this);
    }

    public boolean isMinecraftProfile(String value) {
        return nameResolver(value) != null || uuidResolver(value) != null;
    }

    public boolean isMinecraftProfile(UUID value) {
        return nameResolver(value) != null;
    }

    public boolean invalidateCache() {
        this.cache.clear();
        return this.cache.isEmpty();
    }

    public boolean invalidateByName(String name) {
        if (!cache.containsKey(name)) return false;
        cache.remove(name);
        return true;
    }

    public boolean invalidateByUUID(String uuid) {
        uuid = uuid.replace("-", "");
        String toRemove = null;
        for (String keySet : cache.keySet()) {
            if (!cache.get(keySet).equals(uuid)) continue;
            toRemove = keySet;
            break;
        }
        if (toRemove == null) return false;
        cache.remove(toRemove);
        return true;
    }

    public boolean invalidateByUUID(UUID uuid) {
        return invalidateByUUID(uuid.toString());
    }
}

