package org.example.core.control.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FindBy {
    String id() default "";
    String name() default "";
    String className() default "";
    String css() default "";
    String xpath() default "";
    String linkText() default "";
    String textContains() default "";
    String textExact() default "";
    String tagName() default "";
}
