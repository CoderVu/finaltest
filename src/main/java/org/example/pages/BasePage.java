package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.common.imp.Element;
import org.example.core.control.util.DriverUtils;
import org.example.core.report.ReportManager;
import org.example.core.report.ITestReporter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
public class BasePage {

    protected ITestReporter reporter = ReportManager.getReporter();

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
