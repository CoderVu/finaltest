package org.example.core.report.strategy;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        log.info("Allure report: finish suite {}", context.getSuite().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.debug("Test started: {}", result.getName());
        
        // Allure TestNG listener will automatically create test case
        // We just need to ensure the lifecycle is ready
        // Store test result for later use if needed
        try {
            // Check if Allure TestNG listener has already created test case
            // If not, we'll let it create automatically
            log.debug("Test started - Allure TestNG listener will handle test case creation");
        } catch (Throwable t) {
            log.debug("Error in onTestStart: {}", t.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.debug("Test passed: {}", result.getName());
        // Allure TestNG listener will handle test case status update
        // We just need to ensure steps from @AfterMethod can still be added
    }
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            // check thread-local tracker first, then result attribute
            if (org.example.core.report.FailureTracker.isHandledForCurrentThread()
                    || Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"))) {
                log.debug("Skipping onTestFailure capture (already handled by soft-assert step): {}", result.getName());
                // clear thread marker to avoid affecting subsequent tests on same thread
                org.example.core.report.FailureTracker.clearForCurrentThread();
            }
        } catch (Throwable ignored) {}

        captureScreenshotOnFailure(result, "Failure");
        
        // Allure TestNG listener will handle test case status update
        // We just need to ensure steps from @AfterMethod can still be added
        
        // ensure any leftover thread marker is cleared after handling a hard failure
        try { org.example.core.report.FailureTracker.clearForCurrentThread(); } catch (Throwable ignored) {}
    }
    

    @Override
    public void onTestSkipped(ITestResult result) {
        attachScreenshot("test_skipped_" + result.getName());
        // Allure TestNG listener will handle test case status update
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        attachScreenshot("test_timeout_" + result.getName());
        onTestFailure(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        // Allure TestNG listener will handle test case lifecycle
        // Steps from @AfterMethod/@AfterClass will automatically attach to test case
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

    // Return raw screenshot bytes (no Allure annotations) - central helper to avoid duplicate attachments.
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

    // Obtain WebDriver via DriverManager public API for the current thread.
    private static WebDriver getDriver() {
        try {
            return DriverManager.createWebDriver();
        } catch (Throwable e) {
            log.debug("Unable to obtain WebDriver from DriverManager: {}", e.getMessage());
            return null;
        }
    }
    
    // Simple helper used above
    private static String getCurrentDate(String pattern) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return LocalDateTime.now().toString();
        }
    }
    // Replaced undefined WebDriverRunner usage with local getDriver() and added error message helper
    private void captureScreenshotOnFailure(ITestResult result, String failureType) {
        try {
            WebDriver driver = getDriver();
            if (driver != null) {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (png != null && png.length > 0) {
                    String testName = result.getName();

                    Allure.addAttachment(
                            failureType + " - " + testName,
                            "image/png",
                            new ByteArrayInputStream(png),
                            "png"
                    );

                    log.error("{}: {} - {}", failureType, testName, getErrorMessage(result));
                }
            } else {
                log.debug("No WebDriver available to capture screenshot for {}: {}", failureType, result.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to capture screenshot for {}: {}", failureType, e.getMessage());
        }
    }

    // Helper to safely extract throwable message / stacktrace summary
    private String getErrorMessage(ITestResult result) {
        if (result == null) return "No result info";
        Throwable t = result.getThrowable();
        if (t == null) return "No throwable available";
        String msg = t.getMessage();
        if (msg != null && !msg.trim().isEmpty()) return msg;
        // fallback to class name of throwable
        return t.getClass().getName();
    }
}
