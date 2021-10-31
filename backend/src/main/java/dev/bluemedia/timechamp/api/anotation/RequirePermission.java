package dev.bluemedia.timechamp.api.anotation;

import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to bind the {@link dev.bluemedia.timechamp.api.filter.PermissionFilter} to REST methods.
 *
 * @author Oliver Traber
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequirePermission {
    Permission[] value() default {Permission.READ};
}
