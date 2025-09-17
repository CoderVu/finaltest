package org.example.pages;

import org.example.core.control.common.annotation.FindBy;
import org.example.core.control.common.imp.Element;
import org.openqa.selenium.By;

import java.lang.reflect.Field;

public class PageFactory {

    public static void initElements(Object page) {
        Class<?> clazz = page.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FindBy.class)) {
                FindBy findBy = field.getAnnotation(FindBy.class);
                By locator = buildLocator(findBy);

                if (locator != null) {
                    try {
                        field.setAccessible(true);
                        field.set(page, new Element(locator));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cannot set field: " + field.getName(), e);
                    }
                }
            }
        }
    }

    private static By buildLocator(FindBy findBy) {
        if (!findBy.id().isEmpty()) {
            return By.id(findBy.id());
        } else if (!findBy.name().isEmpty()) {
            return By.name(findBy.name());
        } else if (!findBy.className().isEmpty()) {
            return By.className(findBy.className());
        } else if (!findBy.css().isEmpty()) {
            return By.cssSelector(findBy.css());
        } else if (!findBy.xpath().isEmpty()) {
            return By.xpath(findBy.xpath());
        } else if (!findBy.linkText().isEmpty()) {
            return By.linkText(findBy.linkText());
        } else if (!findBy.textExact().isEmpty()) {
            return By.xpath(String.format("//*[normalize-space(text())='%s']", findBy.textExact()));
        } else if (!findBy.textContains().isEmpty()) {
            return By.xpath(String.format("//*[contains(normalize-space(text()),'%s')]", findBy.textContains()));
        } else if (!findBy.tagName().isEmpty()) {
            return By.tagName(findBy.tagName());
        }
        return null;
    }
}
