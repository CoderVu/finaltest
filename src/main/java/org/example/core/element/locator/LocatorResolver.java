package org.example.core.element.locator;

import org.openqa.selenium.By;

public class LocatorResolver {
    
    private LocatorResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    public static By resolve(String locator) {
        if (locator == null || locator.trim().isEmpty()) {
            throw new IllegalArgumentException("Locator cannot be null or empty");
        }
        
        String body = locator.replaceAll("[\\w\\s]*=(.*)", "$1").trim();
        String type = locator.replaceAll("([\\w\\s]*)=.*", "$1").trim();
        
        switch (type.toLowerCase()) {
            case "css":
                return By.cssSelector(body);
            case "id":
                return By.id(body);
            case "class":
                return By.className(body);
            case "link":
                return By.linkText(body);
            case "xpath":
                return By.xpath(body);
            case "text":
                return By.xpath(String.format("//*[contains(text(), '%s')]", body));
            case "name":
                return By.name(body);
            default:
                return By.xpath(locator);
        }
    }
}

