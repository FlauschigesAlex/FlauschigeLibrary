package at.flauschigesalex.defaultLibrary.database.mongo;

import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoIgnore;
import at.flauschigesalex.defaultLibrary.database.mongo.annotations.MongoInformation;

@MongoInformation
@MongoIgnore
public abstract class MongoInformationClass {

    /**
     * Upon connecting to your MongoDatabase, all classes extending this class will be registered automatically.
     */
    public MongoInformationClass() {
    }
}
