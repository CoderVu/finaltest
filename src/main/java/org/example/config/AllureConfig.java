package org.example.config;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayInputStream;
import java.io.File;
import static org.example.utils.DateUtils.getCurrentDate;

@Slf4j
public class AllureConfig implements ITestListener, IConfigurationListener {

    @BeforeSuite
    public void setupAllureDir() {
        File allureResultsDir = new File("target/allure-results");
        if (!allureResultsDir.exists()) {
            allureResultsDir.mkdirs();
        }
        log.info("Allure results directory setup completed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        captureScreenshotOnFailure(result, "Test Failure");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        captureScreenshotOnFailure(result, "Test Skipped");
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        captureScreenshotOnFailure(result, "Test Timeout");
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.debug("Test started: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.debug("Test passed: {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("Test failed but within success percentage: {}", result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Test suite finished: {}", context.getName());
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        attachScreenshot(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "config_failure_" + 
            (itr.getMethod() != null ? itr.getMethod().getMethodName() : "config"));
        Throwable t = itr.getThrowable();
        if (t != null) {
            Allure.step(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "CONFIG FAIL: " + t.getMessage(), Status.FAILED);
        }
        log.error("Configuration failure: {}", t != null ? t.getMessage() : "Unknown error");
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        log.debug("Configuration success: {}", itr.getMethod() != null ? itr.getMethod().getMethodName() : "config");
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        log.debug("Configuration skipped: {}", itr.getMethod() != null ? itr.getMethod().getMethodName() : "config");
    }

    private void captureScreenshotOnFailure(ITestResult result, String failureType) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            if (driver != null) {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (png != null && png.length > 0) {
                    String timestamp = getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS");
                    String testName = result.getName();
                    String attachmentName = String.format("%s Screenshot [%s] - %s in %s", 
                        failureType, timestamp, testName);
                    
                    Allure.addAttachment(attachmentName, "image/png", new ByteArrayInputStream(png), "png");
                    
                    String errorMessage = getErrorMessage(result);
                    Allure.step(String.format("%s: %s - %s", failureType, testName, errorMessage), 
                        Status.FAILED);
                    
                    log.info("Screenshot captured and attached to Allure report for {}: {}", failureType, testName);
                }
            }
        } catch (Exception ignored) {
            log.warn("Failed to capture screenshot for {}: {}", failureType, ignored.getMessage());
        }
    }

    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            String message = throwable.getMessage();
            if (message != null && !message.trim().isEmpty()) {
                return message;
            }
            return throwable.getClass().getSimpleName();
        }
        return "Unknown error";
    }

    public static boolean isElementRelatedError(Throwable t) {
        if (t == null) return false;
        
        if (t instanceof NoSuchElementException || 
            t instanceof TimeoutException ||
            t instanceof ElementNotInteractableException ||
            t instanceof StaleElementReferenceException) {
            return true;
        }
        
        String message = t.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("no such element") ||
                   lowerMessage.contains("element not found") ||
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("element not interactable") ||
                   lowerMessage.contains("stale element") ||
                   lowerMessage.contains("element is not attached") ||
                   lowerMessage.contains("element click intercepted");
        }
        
        return false;
    }

    public static void captureElementErrorScreenshot(String context, String errorMessage) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            if (driver != null) {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (png != null && png.length > 0) {
                    String timestamp = getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS");
                    String attachmentName = String.format("Element Error Screenshot [%s] - %s", 
                        timestamp, context);
                    
                    Allure.addAttachment(attachmentName, "image/png", new ByteArrayInputStream(png), "png");
                    Allure.step(String.format("Element Error: %s - %s", context, errorMessage), 
                        Status.FAILED);
                    
                    log.info("Element error screenshot captured: {} - {}", context, errorMessage);
                }
            }
        } catch (Exception ignored) {
            log.warn("Failed to capture element error screenshot: {}", ignored.getMessage());
        }
    }

    public static void attachScreenshot(String name) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (bytes != null && bytes.length > 0) {
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
            }
        } catch (Throwable ignored) {
            log.warn("Failed to attach screenshot: {}", ignored.getMessage());
        }
    }

    @Attachment(value = "Attachment", type = "image/png")
    public static byte[] takeScreenshot() {
        return attachScreenshotBytes();
    }

    @Attachment(value = "Attachment", type = "image/png")
    public static byte[] attachScreenshotBytes() {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            if (driver != null) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Throwable ignored) {
            log.warn("Failed to capture screenshot bytes: {}", ignored.getMessage());
        }
        return new byte[0];
    }
}