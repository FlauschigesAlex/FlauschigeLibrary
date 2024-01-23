package at.flauschigesalex.defaultLibrary.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@link ElementType#TYPE} is invisible to local {@link at.flauschigesalex.defaultLibrary.utils.reflections.Reflector reflectors}.<br>
 * Indicates that a {@link ElementType#FIELD} is invisible while {@link Printable printing}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Invisible {
}
