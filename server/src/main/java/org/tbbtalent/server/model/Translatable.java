package org.tbbtalent.server.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface Translatable {

    String value() default "";
    String translation() default "";

}
