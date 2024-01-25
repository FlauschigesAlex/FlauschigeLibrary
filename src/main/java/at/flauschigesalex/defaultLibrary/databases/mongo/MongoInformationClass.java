package at.flauschigesalex.defaultLibrary.databases.mongo;

import at.flauschigesalex.defaultLibrary.databases.mongo.annotations.MongoClass;
import at.flauschigesalex.defaultLibrary.databases.mongo.annotations.MongoIgnore;

@MongoClass
@MongoIgnore
public abstract class MongoInformationClass {

    /**
     * Upon connecting to your MongoDatabase, all classes extending this class will be registered automatically.
     */
    public MongoInformationClass() {
    }
}
