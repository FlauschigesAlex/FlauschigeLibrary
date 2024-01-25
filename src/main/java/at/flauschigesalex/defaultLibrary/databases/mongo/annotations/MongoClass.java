package at.flauschigesalex.defaultLibrary.databases.mongo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-Registers a {@link Class class} to a {@link at.flauschigesalex.defaultLibrary.databases.mongo.MongoDatabaseManager#register(Class[]) MongoDatabaseManager}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MongoClass {
}
