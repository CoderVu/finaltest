package org.example.core.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static Element $(Element parent, String locator) {
        return new Element(parent, locator);
    }

    public static Element $(Element parent, By locator) {
        return new Element(parent, locator);
    }

    public static Element $(Element parent, String locator, Object... args) {
        return new Element(parent, locator, args);
    }

    public static List<Element> $$(String locator) {
        Element element = new Element(locator);
        List<org.openqa.selenium.WebElement> webElements = element.getElements();
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new Element(String.format("(%s)[%d]", locator, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<Element> $$(By locator) {
        Element element = new Element(locator);
        List<WebElement> webElements = element.getElements();
        String locatorStr = locator.toString().replace("By.xpath: ", "");
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new Element(String.format("(%s)[%d]", locatorStr, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<Element> $$(Element parent, String childLocator) {
        List<WebElement> webElements = parent.getChildElements(childLocator);
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new Element(parent, String.format("(%s)[%d]", childLocator, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<Element> $$(Element parent, String locator, String childLocator) {
        Element element = new Element(parent, locator);
        List<WebElement> webElements = element.getChildElements(childLocator);
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new Element(element, String.format("(%s)[%d]", childLocator, i + 1)))
                .collect(Collectors.toList());
    }
}