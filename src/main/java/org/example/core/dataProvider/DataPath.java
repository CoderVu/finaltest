package org.example.core.dataProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a test method parameter to bind it from the JSON dataset by path.
 * Examples:
 *   @DataPath("destination") String destination
 *   @DataPath("occupancy.rooms") int rooms
 *   @DataPath("validation") JsonObject validation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DataPath {
    String value();
}


