package org.example.pages;

import com.codeborne.selenide.Condition;
import lombok.extern.slf4j.Slf4j;
import org.example.core.control.common.imp.Element;
import org.example.core.control.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class BasePage {

    protected WebElement findElement(By locator) {
        return DriverUtils.getWebDriver().findElement(locator);
    }

    protected List<WebElement> findElements(By locator) {
        return DriverUtils.getWebDriver().findElements(locator);
    }

    protected void waitForElementVisible(Element element) {
        element.waitForDisplay();
    }

    protected void clickElement(Element element) {
        element.waitForDisplay();
        element.click();
    }

    protected void enterText(Element element, String text) {
        element.waitForDisplay();
        element.clear();
        element.setText(text);
    }
}
