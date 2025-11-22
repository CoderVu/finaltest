package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.util.DriverUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.example.core.element.util.DriverUtils.getWebDriver;

/**
 * Simple Element Helper class - no complex layers
 * Direct methods for common Selenium operations
 */
@Slf4j
public class ElementHelper {

    private static WebDriverWait getWait() {
        return new WebDriverWait(getWebDriver(), DriverUtils.getTimeOut());
    }

    public static void maximizeBrowser() {
        getWebDriver().manage().window().maximize();
        log.info("Browser is Maximized");
    }

    public static void clickOn(By locator) {
        log.info("Waiting for element to be clickable");
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
        log.info("Click operation is performed");
    }

    public static void clickOnCheckBox(By locator) {
        log.info("Waiting for element to be visible");
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.click();
        log.info("Click operation is performed on checkBox");
    }

    public static void clickOn(WebElement element) {
        getWait().until(ExpectedConditions.elementToBeClickable(element)).click();
        log.info("Click operation is performed");
    }

    public static void enterText(By locator, String textToEnter) {
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.sendKeys(textToEnter);
        log.info("Text Entered in the text box is " + textToEnter);
    }

    public static void enterSpecialKey(By locator, Keys keys) {
        WebElement element = getWebDriver().findElement(locator);
        element.sendKeys(keys);
        log.info("Key Entered in the text box is " + keys);
    }

    public static void clearText(By locator) {
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        log.info("TextBox clean operation is performed");
    }

    public static String getVisibleText(By locator) {
        WebElement element = getWebDriver().findElement(locator);
        log.info("element is going to retrieve");
        return element.getText();
    }

    public static String getVisibleText(WebElement webElement) {
        log.info("element is going to retrieve");
        return webElement.getText();
    }

    public static List<String> getAllVisibleText(By locator) {
        List<WebElement> webElementList = getWebDriver().findElements(locator);
        log.info("element are going to retrieve from list");
        List<String> productList = new ArrayList<>();
        for (WebElement element : webElementList) {
            productList.add(getVisibleText(element));
        }
        return productList;
    }

    public static List<WebElement> getAllVisibleElements(By locator) {
        List<WebElement> webElementList = getWebDriver().findElements(locator);
        log.info("element are going to retrieve from list");
        return webElementList;
    }

    public static void selectFromDropDownData(By locator, String optionToSelect) {
        log.info("Selecting visible text from the dropdown");
        WebElement element = getWebDriver().findElement(locator);
        Select select = new Select(element);
        select.selectByVisibleText(optionToSelect);
        log.info(optionToSelect + " is selected from the dropdown");
    }

    public static String takeScreenShot(String name) {
        TakesScreenshot takesScreenshot = (TakesScreenshot) getWebDriver();
        log.info("driver is upcasted to TakeScreenShot");
        File src = takesScreenshot.getScreenshotAs(OutputType.FILE);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH-mm-ss");
        String timeStamp = format.format(date);
        
        File screenshotDir = new File("./screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }
        
        String desPath = "./screenshots/" + name + " - " + timeStamp + ".png";
        File des = new File(desPath);
        try {
            java.nio.file.Files.copy(src.toPath(), des.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save screenshot: " + e.getMessage(), e);
        }
        return desPath;
    }
}

