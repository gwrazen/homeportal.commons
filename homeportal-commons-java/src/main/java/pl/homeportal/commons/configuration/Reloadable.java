package pl.homeportal.commons.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @Value} field that {@link PropertiesReloader} may replace in a running application.
 * Only put it on values read through a getter at every use — a consumer that copies the value
 * at startup will not see the change.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reloadable
{
}
