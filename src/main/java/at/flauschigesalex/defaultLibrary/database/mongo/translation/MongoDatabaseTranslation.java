package at.flauschigesalex.defaultLibrary.database.mongo.translation;

import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoInformation;
import at.flauschigesalex.defaultLibrary.utils.Silent;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonIgnore;

@Getter
@Silent
@MongoInformation
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
    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    @Deprecated
    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
