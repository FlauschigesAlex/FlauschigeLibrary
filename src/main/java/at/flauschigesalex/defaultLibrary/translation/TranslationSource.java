package at.flauschigesalex.defaultLibrary.translation;

import at.flauschigesalex.defaultLibrary.database.mongo.MongoDatabaseManager;
import at.flauschigesalex.defaultLibrary.database.mongo.translation.MongoDatabaseTranslation;
import at.flauschigesalex.defaultLibrary.utils.file.FileManager;
import at.flauschigesalex.defaultLibrary.utils.file.JsonManager;
import at.flauschigesalex.defaultLibrary.utils.file.ResourceManager;
import com.mongodb.client.MongoCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class TranslationSource {

    static Locale defaultLocale = Locale.US;
    protected final HashMap<Locale, JsonManager> map = new HashMap<>();

    protected TranslationSource() {
    }

    static TranslationSource byDatabase(final @NotNull MongoDatabaseManager database) {
        final TranslationSource source = new TranslationSource();
        final ArrayList<String> list = new ArrayList<>();
        database.getMongoDatabase().listCollectionNames().forEach(list::add);

        for (final Locale locale : Locale.getAvailableLocales()) {
            if (!list.contains(locale.toLanguageTag())) continue;
            final MongoCollection<MongoDatabaseTranslation> collection = database.getCollection(locale.toLanguageTag(), MongoDatabaseTranslation.class);
            final HashMap<Locale, JsonManager> translations = new HashMap<>();

            for (MongoDatabaseTranslation translation : collection.find()) {
                translations.putIfAbsent(locale, JsonManager.parse("{}"));
                translations.get(locale).write(translation.getTranslationKey(), translation.getTranslation());
            }
            translations.forEach(source::insert);
        }
        return source;
    }

    static TranslationSource byFile(final @Nullable String sourcePath) {
        final TranslationSource source = new TranslationSource();
        for (final Locale locale : Locale.getAvailableLocales()) {
            String path = "";
            if (sourcePath != null)
                path += sourcePath + "/";
            path += locale.toLanguageTag() + ".json";

            final FileManager manager = FileManager.getFile(path);
            if (!manager.getFile().exists()) continue;

            final JsonManager jsonManager = manager.getJsonManager();
            if (jsonManager == null) continue;

            source.insert(locale, jsonManager);
        }
        return source;
    }

    static TranslationSource byResource(final @Nullable String sourcePath) {
        final TranslationSource source = new TranslationSource();
        for (final Locale locale : Locale.getAvailableLocales()) {
            String path = "";
            if (sourcePath != null)
                path += sourcePath + "/";
            path += locale.toLanguageTag() + ".json";

            final ResourceManager manager = ResourceManager.getResource(path);
            if (manager == null) continue;

            final JsonManager jsonManager = manager.getJsonManager();
            if (jsonManager == null) continue;

            source.insert(locale, jsonManager);
        }
        return source;
    }

    protected final void insert(final @NotNull Locale locale, final @NotNull JsonManager jsonManager) {
        map.put(locale, jsonManager);
    }

    protected final @Nullable JsonManager byLocale(final @NotNull Locale locale) {
        return map.get(locale);
    }

    protected final JsonManager defaultLocale() {
        return map.get(defaultLocale);
    }
}
