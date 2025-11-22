package org.example.core.element.factory;

import org.example.core.element.IElementWrapper;
import org.example.core.element.ElementWrapperWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * Factory method for creating IElementWrapper instances with optional dynamic locators.
 *
 * Usage examples:
 *
 * 1. Basic usage (no dynamic args):
 *    $(By.xpath("//button[@id='submit']"));
 *    $(By.id("username"));
 *    $(By.cssSelector("div.container"));
 *    $(By.className("btn-primary"));
 *
 * 2. Dynamic locator with XPath:
 *    $(By.xpath("//button[@id='%s']"), "submit-btn");
 *    // => //button[@id='submit-btn']
 *
 *    $(By.xpath("//div[@id='%s']//button[@class='%s']"), "container", "submit");
 *    // => //div[@id='container']//button[@class='submit']
 *
 * 3. Dynamic locator with ID / CSS / ClassName:
 *    $(By.id("user-%s-input"), "123");
 *    $(By.cssSelector("button.btn-%s"), "primary");
 *    $(By.className("btn-%s"), "success");
 */

public class ElementFactory {

    private ElementFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static IElementWrapper $(By locator) {
        return new ElementWrapperWrapper(locator);
    }

    public static IElementWrapper $(By locator, Object... args) {
        String locatorString = extractLocatorString(locator);
        String locatorType = extractLocatorType(locator);
        return new ElementWrapperWrapper(locatorString, locatorType, args);
    }
    
    private static String extractLocatorString(By by) {
        String byString = by.toString();
        int colonIndex = byString.indexOf(':');
        if (colonIndex > 0 && colonIndex < byString.length() - 1) {
            return byString.substring(colonIndex + 1).trim();
        }
        return byString;
    }
    
    private static String extractLocatorType(By by) {
        String byString = by.toString();
        if (byString.startsWith("By.xpath:")) {
            return "xpath";
        } else if (byString.startsWith("By.id:")) {
            return "id";
        } else if (byString.startsWith("By.cssSelector:")) {
            return "cssSelector";
        } else if (byString.startsWith("By.className:")) {
            return "className";
        } else if (byString.startsWith("By.name:")) {
            return "name";
        } else if (byString.startsWith("By.tagName:")) {
            return "tagName";
        } else if (byString.startsWith("By.linkText:")) {
            return "linkText";
        } else if (byString.startsWith("By.partialLinkText:")) {
            return "partialLinkText";
        }
        return "xpath";
    }

    public static List<IElementWrapper> $$(By locator) {
        IElementWrapper element = new ElementWrapperWrapper(locator);
        List<WebElement> webElements = element.getElements();
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new ElementWrapperWrapper(locator))
                .collect(Collectors.toList());
    }

    public static List<IElementWrapper> $$(By locator, Object... args) {
        String locatorString = extractLocatorString(locator);
        String locatorType = extractLocatorType(locator);
        IElementWrapper element = new ElementWrapperWrapper(locatorString, locatorType, args);
        List<WebElement> webElements = element.getElements();
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new ElementWrapperWrapper(locatorString, locatorType, args))
                .collect(Collectors.toList());
    }
}

