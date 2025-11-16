package org.example.core.control.element;

import org.example.core.control.base.imp.BaseControl;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Similar to Selenide's $() pattern.
 * 
 * Usage:
 * <pre>
 * import static org.example.core.control.ElementFactory.ElementFactory.$;
 * 
 * Element button = $("//button[@id='submit']");
 * Element input = $(By.id("username"));
 * Element child = $(parent, ".//span");
 * </pre>
 */
public class ElementFactory {

    private ElementFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Element $(String locator) {
        return new Element(locator);
    }

    public static Element $(By locator) {
        return new Element(locator);
    }

    public static Element $(String locator, Object... args) {
        return new Element(locator, args);
    }

    public static Element $(BaseControl parent, String locator) {
        return new Element(parent, locator);
    }

    public static Element $(BaseControl parent, By locator) {
        return new Element(parent, locator);
    }

    public static Element $(BaseControl parent, String locator, Object... args) {
        return new Element(parent, locator, args);
    }

    public static List<Element> $$(String locator) {
        Element element = new Element(locator);
        return element.getListElements(Element.class);
    }

    public static List<Element> $$(By locator) {
        Element element = new Element(locator);
        return element.getListElements(Element.class);
    }

    public static List<Element> $$(BaseControl parent, String childLocator) {
        return parent.getListElements(Element.class, childLocator);
    }

    public static List<Element> $$(BaseControl parent, String locator, String childLocator) {
        Element element = new Element(parent, locator);
        return element.getListElements(Element.class, childLocator);
    }
}

