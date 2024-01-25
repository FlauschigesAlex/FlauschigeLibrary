package at.flauschigesalex.defaultLibrary.databases.mongo.translation;

import at.flauschigesalex.defaultLibrary.databases.mongo.annotations.MongoClass;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.jetbrains.annotations.NotNull;

@Getter
@MongoClass
@SuppressWarnings("unused")
public final class MongoDatabaseTranslation {

    private String translationKey;
    private String translation;

    @Deprecated
    public MongoDatabaseTranslation() {
    }

    @BsonIgnore
    public String getValue() {
        return translation;
    }

    @Deprecated
    public void setTranslationKey(final @NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    @Deprecated
    public void setTranslation(final @NotNull String translation) {
        this.translation = translation;
    }
}
