package at.flauschigesalex.defaultLibrary.translation;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Locale;

@Getter(AccessLevel.PACKAGE)
@SuppressWarnings("unused")
public final class TranslationCache {

    private static final ArrayList<TranslationCache> blacklist = new ArrayList<>();
    private static final ArrayList<TranslationCache> cache = new ArrayList<>();
    private final Locale locale;
    private final String key;
    private String value;

    private TranslationCache(final @NotNull Locale locale, final @NotNull String key) {
        this.locale = locale;
        this.key = key;
    }

    public static void clear() {
        cache.clear();
    }

    public static void clear(final @NotNull Locale locale, final @NotNull String value) {
        clear(new TranslationCache(locale, value));
    }

    public static void clear(final @NotNull TranslationCache translationCache) {
        while (cache.contains(translationCache))
            cache.remove(translationCache);
    }

    public static void blacklist(final @NotNull Locale locale, final @NotNull String value) {
        clear(locale, value);
        final TranslationCache translationCache = new TranslationCache(locale, value);
        if (!blacklist.contains(translationCache))
            blacklist.add(translationCache);
    }

    public static @Nullable TranslationCache getCached(final @NotNull Locale locale, final @NotNull String key) {
        for (final TranslationCache translationCache : cache) {
            if (translationCache.locale.equals(locale) && translationCache.key.equals(key))
                return translationCache;
        }
        return null;
    }

    static void cache(final @NotNull Locale locale, final @NotNull String key, final @NotNull String value) {
        final TranslationCache translationCache = new TranslationCache(locale, key).value(value);
        if (blacklist.contains(translationCache))
            return;
        if (!cache.contains(translationCache))
            cache.add(translationCache);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TranslationCache translationCache)) return false;
        return translationCache.locale.equals(locale) && translationCache.key.equals(key);
    }

    TranslationCache value(final @NotNull String value) {
        this.value = value;
        clear(this);
        cache.add(this);
        return this;
    }
}
