package at.flauschigesalex.defaultLibrary.databases.mongo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@link Class class} cannot be registered to a {@link at.flauschigesalex.defaultLibrary.databases.mongo.MongoDatabaseManager MongoDatabaseManager}.<br>
 * This includes {@link at.flauschigesalex.defaultLibrary.databases.mongo.MongoDatabaseManager#register(Class[]) manually registered classes}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MongoIgnore {
}
