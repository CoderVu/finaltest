package org.example.core.report.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Step annotation to log steps similar to Selenide/Allure @Step.
 * Usage: annotate methods you want to appear as steps in the active reporter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Step {
    /** Optional message template. You can use {argN} placeholders for arguments. */
    String value() default "";
}


