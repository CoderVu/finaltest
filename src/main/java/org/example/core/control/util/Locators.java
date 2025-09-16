package org.example.core.control.util;

import org.openqa.selenium.By;

public final class Locators {

    private Locators() {}

    public static By css(String selector) {
        return By.cssSelector(selector);
    }

    public static By xpath(String expression) {
        return By.xpath(expression);
    }

    public static By id(String id) {
        return By.id(id);
    }

    public static By name(String name) {
        return By.name(name);
    }

    public static By className(String className) {
        return By.className(className);
    }

    public static By linkText(String linkText) {
        return By.linkText(linkText);
    }

    public static By textContains(String text) {
        return By.xpath(String.format("//*[contains(normalize-space(text()), '%s')]", text));
    }

    public static By textExact(String text) {
        return By.xpath(String.format("//*[normalize-space(text())='%s']", text));
    }
}


