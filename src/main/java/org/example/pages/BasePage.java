package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.common.imp.Element;
import org.example.core.control.util.DriverUtils;
import org.example.report.ReporterFactory;
import org.example.report.TestReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
public class BasePage {

    // Unified reporter for all Page Objects - works with Allure, Extent, or Jenkins
    protected TestReporter reporter = ReporterFactory.getInstance();

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
