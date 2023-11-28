package at.flauschigesalex.flauschigeAPI.database.mongo;

import at.flauschigesalex.flauschigeAPI.database.mongo.annotations.MongoIgnore;
import at.flauschigesalex.flauschigeAPI.database.mongo.annotations.MongoInformation;

@MongoInformation
@MongoIgnore
public abstract class MongoInformationClass {

    /**
     * Upon connecting to your MongoDatabase, all classes extending this class will be registered automatically.
     */
    public MongoInformationClass() {}
}
