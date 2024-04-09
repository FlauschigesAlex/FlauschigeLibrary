package at.flauschigesalex.defaultLibrary.minecraft.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings({"unused"})
public final class MojangAPI {

    private static MojangAPI mojangAPI;
    final HashMap<String, String> cache = new HashMap<>();

    private MojangAPI() {
    }

    public static MojangAPI mojangAPI() {
        if (mojangAPI == null) mojangAPI = new MojangAPI();
        return mojangAPI;
    }

    @CheckReturnValue
    public NameResolver resolveName(final @Nullable UUID uuid) {
        return new NameResolver(uuid).api(this);
    }

    @CheckReturnValue
    public NameResolver resolveName(final @Nullable String uuid) {
        return new NameResolver(uuid).api(this);
    }

    @CheckReturnValue
    public UUIDResolver resolveUUID(final @Nullable String name) {
        return new UUIDResolver(name).api(this);
    }

    @CheckReturnValue
    public NameCorrection correctName(final @Nullable String name) {
        return new NameCorrection(name).instanced(this);
    }

    public boolean isMinecraftProfile(final @NotNull String value) {
        return resolveName(value).resolve() != null || resolveUUID(value).resolve() != null;
    }

    public boolean isMinecraftProfile(final @NotNull UUID value) {
        return resolveName(value).resolve() != null;
    }

    public void invalidateCache() {
        this.cache.clear();
    }

    public boolean invalidateByName(final @Nullable String name) {
        if (!cache.containsKey(name)) return false;
        return cache.remove(name) != null;
    }

    public boolean invalidateByUUID(@Nullable String uuid) {
        if (uuid == null)
            return false;
        uuid = uuid.replace("-", "");
        String toRemove = null;
        for (final String keySet : cache.keySet()) {
            if (!cache.get(keySet).equals(uuid)) continue;
            toRemove = keySet;
            break;
        }
        if (toRemove == null) return false;
        return cache.remove(toRemove) != null;
    }

    public boolean invalidateByUUID(final @Nullable UUID uuid) {
        if (uuid == null)
            return false;
        return invalidateByUUID(uuid.toString());
    }
}

