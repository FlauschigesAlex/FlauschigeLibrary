package at.flauschigesalex.defaultLibrary.utils;

import at.flauschigesalex.defaultLibrary.any.Reflector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@link Class class} is not visible to local {@link Reflector reflectors}.<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface Invisible {
}
