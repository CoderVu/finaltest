package org.example.utils;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.SearchContext;


public class WebDriverUtils {

    public static String getCurrentUrl() {
        return WebDriverRunner.getWebDriver().getCurrentUrl();
    }
    public static SearchContext getWebDriver() {
        return WebDriverRunner.getWebDriver();
    }

}