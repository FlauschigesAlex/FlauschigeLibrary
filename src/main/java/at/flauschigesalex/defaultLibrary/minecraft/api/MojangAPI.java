package at.flauschigesalex.defaultLibrary.minecraft.api;

import at.flauschigesalex.defaultLibrary.utils.Printable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.annotation.CheckReturnValue;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings({"unused", "ConstantValue"})
public final class MojangAPI extends Printable {

    private static MojangAPI mojangAPI;

    public static MojangAPI mojangAPI() {
        if (mojangAPI == null) mojangAPI = new MojangAPI();
        return mojangAPI;
    }
    final HashMap<String, String> cache = new HashMap<>();

    private MojangAPI() {
    }

    @CheckReturnValue
    public NameResolver nameResolver(final @Nullable UUID uuid) {
        return new NameResolver(uuid).instanced(this);
    }

    @CheckReturnValue
    public NameResolver nameResolver(final @Nullable String uuid) {
        return new NameResolver(uuid).instanced(this);
    }

    @CheckReturnValue
    public UUIDResolver uuidResolver(final @Nullable String name) {
        return new UUIDResolver(name).instanced(this);
    }

    @CheckReturnValue
    public NameCorrection nameCorrection(final @Nullable String name) {
        return new NameCorrection(name).instanced(this);
    }

    public boolean isMinecraftProfile(final @NotNull String value) {
        return nameResolver(value) != null || uuidResolver(value) != null;
    }

    public boolean isMinecraftProfile(final @NotNull UUID value) {
        return nameResolver(value) != null;
    }

    public boolean invalidateCache() {
        this.cache.clear();
        return this.cache.isEmpty();
    }

    public boolean invalidateByName(final @Nullable String name) {
        if (!cache.containsKey(name)) return false;
        cache.remove(name);
        return true;
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
        cache.remove(toRemove);
        return true;
    }

    public boolean invalidateByUUID(final @Nullable UUID uuid) {
        if (uuid == null)
            return false;
        return invalidateByUUID(uuid.toString());
    }
}

