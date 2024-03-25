package at.flauschigesalex.defaultLibrary.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@link Class class} is not visible to local {@link at.flauschigesalex.defaultLibrary.reflections.Reflector reflectors}.<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Invisible {
}
