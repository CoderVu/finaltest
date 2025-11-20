package org.example.core.element.factory;

import org.example.core.element.IElement;
import org.example.core.element.WebElementWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElementFactory {

    private ElementFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static IElement $(String locator) {
        return new WebElementWrapper(locator);
    }

    public static IElement $(By locator) {
        return new WebElementWrapper(locator);
    }

    public static IElement $(String locator, Object... args) {
        return new WebElementWrapper(locator, args);
    }

    public static IElement $(IElement parent, String locator) {
        return new WebElementWrapper(parent, locator);
    }

    public static IElement $(IElement parent, By locator) {
        return new WebElementWrapper(parent, locator);
    }

    public static IElement $(IElement parent, String locator, Object... args) {
        return new WebElementWrapper(parent, locator, args);
    }

    public static List<IElement> $$(String locator) {
        IElement element = new WebElementWrapper(locator);
        List<WebElement> webElements = element.getElements();
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new WebElementWrapper(String.format("(%s)[%d]", locator, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<IElement> $$(By locator) {
        IElement element = new WebElementWrapper(locator);
        List<WebElement> webElements = element.getElements();
        String locatorStr = locator.toString().replace("By.xpath: ", "");
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new WebElementWrapper(String.format("(%s)[%d]", locatorStr, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<IElement> $$(IElement parent, String childLocator) {
        List<WebElement> webElements = parent.getChildElements(childLocator);
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new WebElementWrapper(parent, String.format("(%s)[%d]", childLocator, i + 1)))
                .collect(Collectors.toList());
    }

    public static List<IElement> $$(IElement parent, String locator, String childLocator) {
        IElement element = new WebElementWrapper(parent, locator);
        List<WebElement> webElements = element.getChildElements(childLocator);
        return IntStream.range(0, webElements.size())
                .mapToObj(i -> new WebElementWrapper(element, String.format("(%s)[%d]", childLocator, i + 1)))
                .collect(Collectors.toList());
    }
}

