package org.example.config;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.example.utils.DateUtils.getCurrentDate;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

import com.codeborne.selenide.WebDriverRunner;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.model.Status;

public class AllureConfig implements ITestListener, IConfigurationListener {

    @BeforeSuite
    public void setupAllureDir() {
        File allureResultsDir = new File("target/allure-results");
        if (!allureResultsDir.exists()) {
            allureResultsDir.mkdirs();
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
        }
        return new byte[0];
    }

    @Override
    public void onTestFailure(ITestResult result) {
        
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachScreenshot(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + result.getName());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        attachScreenshot(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + result.getName());
    }

    public void onConfigurationFailure(ITestResult itr) {
        attachScreenshot(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "config_failure_" + (itr.getMethod() != null ? itr.getMethod().getMethodName() : "config"));
        Throwable t = itr.getThrowable();
        if (t != null) {
            Allure.step(getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "CONFIG FAIL: " + t.getMessage(), Status.FAILED);
        }
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
    }

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }
}