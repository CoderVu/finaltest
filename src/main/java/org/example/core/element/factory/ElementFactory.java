package org.example.core.element.factory;

import lombok.experimental.UtilityClass;
import org.example.core.element.ElementWrapper;
import org.openqa.selenium.By;
/**
 * Factory method for creating ElementWrapper instances with optional dynamic locators.
 *
 * Usage examples:
 *
 * 1. Basic usage (no dynamic args):
 *    $("//button[@id='submit']");
 *    $("//input[@id='username']");
 *    $("//div[contains(@class,'container')]");
 *    $("//span[contains(@class,'btn-primary')]");
 *
 * 2. Dynamic locator with XPath:
 *    $("//button[@id='%s']", "submit-btn");
 *    // => //button[@id='submit-btn']
 *
 *    $("//div[@id='%s']//button[@class='%s']", "container", "submit");
 *    // => //div[@id='container']//button[@class='submit']
 */
@UtilityClass
public class ElementFactory {

    public static ElementWrapper $(By locator) {
        return new ElementWrapper(locator);
    }

    public static ElementWrapper $(String xpath) {
        return new ElementWrapper(xpath);
    }

    public static ElementWrapper $(String xpath, Object... args) {
        return new ElementWrapper(xpath, args);
    }

    public static ElementWrapper $$(String xpath) {
        return new ElementWrapper(xpath);
    }

    public static ElementWrapper $$(String xpath, Object... args) {
        return new ElementWrapper(xpath, args);
    }
}

