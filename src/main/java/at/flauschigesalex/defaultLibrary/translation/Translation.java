package at.flauschigesalex.defaultLibrary.translation;

import at.flauschigesalex.defaultLibrary.database.mongo.MongoDatabaseManager;
import at.flauschigesalex.defaultLibrary.utils.file.JsonManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
public final class Translation {

    @Getter
    private final TranslationSource source;
    private final HashMap<Integer, Object> replacements = new HashMap<>();
    private boolean useFallbackMessage = true;
    private boolean clearReplacements = true;
    private @Deprecated int replacement = 0;

    @Deprecated
    public Translation(final @NotNull TranslationSource translationSource) {
        source = translationSource;
        this.checkArguments();
    }

    public Translation(final @Nullable File file) {
        if (file == null) {
            this.source = TranslationSource.byFile(null);
            this.checkArguments();
            return;
        }
        this.source = TranslationSource.byFile(file.getPath());
        this.checkArguments();
    }

    public Translation(final @Nullable String path) {
        this(path, false);
        this.checkArguments();
    }

    public Translation(final @NotNull MongoDatabaseManager database) {
        this.source = TranslationSource.byDatabase(database);
        this.checkArguments();
    }

    private Translation(final @Nullable String path, final boolean file) {
        if (file) {
            this.source = TranslationSource.byFile(path);
            this.checkArguments();
            return;
        }
        this.source = TranslationSource.byResource(path);
        this.checkArguments();
    }

    @Deprecated
    public Translation useFallbackMessage(final boolean useFallbackMessage) {
        this.useFallbackMessage = useFallbackMessage;
        return this;
    }

    @Deprecated
    public Translation clearReplacements(final boolean clearReplacements) {
        this.clearReplacements = clearReplacements;
        return this;
    }

    public Translation clearReplacements() {
        this.replacements.clear();
        return this;
    }

    public Translation replace(final @NotNull Object replacement) {
        return replace(this.getReplacement(), replacement);
    }

    public Translation replace(final int toReplace, final @NotNull Object replacement) {
        this.replacements.put(toReplace, replacement);
        return this;
    }

    public String translate(final @NotNull Locale locale, final @NotNull Object object) {
        final TranslationCache cache = TranslationCache.getCached(locale, object.toString());
        if (cache != null) {
            final String[] cachedTranslation = new String[]{cache.getValue()};

            replacements.forEach((position, replacement) -> cachedTranslation[0] = cachedTranslation[0].replace("{" + position + "}", replacement.toString()));
            if (clearReplacements)
                replacements.clear();
            return cachedTranslation[0];
        }

        final JsonManager jsonManager = getSource().byLocale(locale);
        if (jsonManager == null) {
            if (useFallbackMessage) {
                final String[] fallbackTranslation = new String[]{getSource().defaultLocale().asString(object.toString())};
                if (fallbackTranslation[0] == null)
                    return object.toString();

                replacements.forEach((position, replacement) -> fallbackTranslation[0] = fallbackTranslation[0].replace("{" + position + "}", replacement.toString()));
                if (clearReplacements)
                    replacements.clear();
                return fallbackTranslation[0];
            }
            return object.toString();
        }
        final String[] translation = new String[]{jsonManager.asString(object.toString())};
        if (translation[0] == null)
            return object.toString();

        replacements.forEach((position, replacement) -> translation[0] = translation[0].replace("{" + position + "}", replacement.toString()));
        if (clearReplacements)
            replacements.clear();

        TranslationCache.cache(locale, object.toString(), translation[0]);
        return translation[0];
    }

    private void checkArguments() {
        if (getSource().byLocale(TranslationSource.defaultLocale) == null)
            throw TranslationException.DefaultLocaleAbsent;
    }

    private int getReplacement() {
        int current = replacement;
        replacement++;
        return current;
    }

    @Deprecated
    public void setDefaultLocale(final @NotNull Locale locale) {
        TranslationSource.defaultLocale = locale;
    }
}
