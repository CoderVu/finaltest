package org.example.core.report.strategy;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.AbstractDriverManager;
import org.example.core.driver.DriverFactory;
import org.example.enums.BrowserType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.core.control.util.DriverUtils.getDriver;
import static org.example.core.report.FailureTracker.clearForCurrentThread;
import static org.example.core.report.FailureTracker.isHandledForCurrentThread;

@Slf4j
public class AllureStrategy implements ReportStrategy {

    @Override
    public void onStart(ITestContext context) {
        File allureResultsDir = new File("target/allure-results");
        if (!allureResultsDir.exists()) {
            allureResultsDir.mkdirs();
        }
        log.info("Allure report: start suite {}", context.getSuite().getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.debug("Allure report: finish suite {}", context.getSuite().getName());
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
    public void onTestFailure(ITestResult result) {
        try {
            if (isHandledForCurrentThread()
                    || Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"))) {
                log.debug("Skipping onTestFailure capture (already handled by soft-assert step): {}", result.getName());
                clearForCurrentThread();
            }
        } catch (Throwable ignored) {
        }

        try {
            clearForCurrentThread();
        } catch (Throwable ignored) {
        }
    }


    @Override
    public void onTestSkipped(ITestResult result) {
        log.debug("Test skipped: {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.debug("Test failed within success percentage: {}", result.getName());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        attachScreenshot("test_timeout_" + result.getName());
        onTestFailure(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        log.debug("Configuration method completed: {}", itr.getMethod().getMethodName());
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
    public void onConfigurationSkip(ITestResult itr) {
    }

    public static void attachScreenshot(String name) {
        try {
            byte[] bytes = getScreenshotBytes();
            if (bytes != null && bytes.length > 0) {
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
            }
        } catch (Throwable ignored) {
        }
    }

    private static byte[] getScreenshotBytes() {
        try {
            WebDriver driver = getDriver();
            if (driver != null && driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Throwable ignored) {
        }
        return new byte[0];
    }

    /**
     * Instance-level implementation for ReportStrategy.failStep.
     * Create a failed step and attach a single screenshot via Allure lifecycle.
     */
    @Override
    public void failStep(String name) {
        String stepUuid = java.util.UUID.randomUUID().toString();
        try {
            io.qameta.allure.model.StepResult sr = new io.qameta.allure.model.StepResult()
                    .setName(name)
                    .setStatus(Status.FAILED);

            Allure.getLifecycle().startStep(stepUuid, sr);

            try {
                byte[] bytes = getScreenshotBytes();
                if (bytes != null && bytes.length > 0) {
                    Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", new ByteArrayInputStream(bytes));
                }
            } catch (Throwable t) {
                log.debug("Failed to capture screenshot for failed step '{}': {}", name, t.getMessage());
            }

            Allure.getLifecycle().stopStep(stepUuid);
        } catch (IllegalStateException | IllegalThreadStateException e) {
            log.debug("Cannot create Allure failed step (no active lifecycle): {}", name);
        } catch (Throwable e) {
            log.debug("Unexpected error while creating Allure failed step '{}': {}", name, e.getMessage());
        }
    }

    private static String getCurrentDate(String pattern) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return LocalDateTime.now().toString();
        }
    }

}
