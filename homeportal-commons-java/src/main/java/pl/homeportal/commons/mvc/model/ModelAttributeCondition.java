package pl.homeportal.commons.mvc.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelAttributeCondition
{
    String prefixUri() default "";
    String [] allowedUris() default {};
}
