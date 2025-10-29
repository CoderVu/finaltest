package org.example.core.dataProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify data file path for test methods
 * 
 * Usage:
 * @Test(dataProvider = "auto", dataProviderClass = DataProvider.class)
 * @DataFile("src/test/resources/data/tc01.json")
 * public void TC01_SearchAndSortHotelSuccessfully(JsonObject data) { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataFile {
    /** Path to the JSON data file */
    String value();
}

