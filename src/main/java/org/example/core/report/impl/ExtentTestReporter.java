package org.example.core.report.impl;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ITestReporter;
import org.example.core.report.strategy.ExtentStrategy;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.core.control.util.DriverUtils.getDriver;

@Slf4j
public class ExtentTestReporter implements ITestReporter {
    private static final Map<Long, ExtentTest> THREAD_TO_TEST = new ConcurrentHashMap<>();

    public static void setTest(ExtentTest test) {
        THREAD_TO_TEST.put(Thread.currentThread().getId(), test);
    }
    public static void clearTest() {
        THREAD_TO_TEST.remove(Thread.currentThread().getId());
    }
    private ExtentTest getCurrentTest() {
        return THREAD_TO_TEST.get(Thread.currentThread().getId());
    }
    @Override
    public void logStep(String message) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.info("STEP: " + message);
        } else {
            log.debug("No ExtentTest found, logging to console: {}", message);
        }
    }
    @Override
    public void info(String message) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.info(message);
        } else {
            log.info(message);
        }
    }
    @Override
    public void logFail(String message, Throwable error) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            if (error != null) {
                test.fail(message + " - " + error.getMessage());
            } else {
                test.fail(message);
            }
        } else {
            log.error("FAIL: {} - {}", message, error != null ? error.getMessage() : "No error");
        }
    }
    @Override
    public void attachScreenshot(String name) {
        ExtentTest test = getCurrentTest();
        try {
            WebDriver driver = getDriver();
            if (driver != null && (driver instanceof TakesScreenshot)) {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

                // Prefer saving screenshots into the same timestamped report directory if available.
                // Save into a "screenshots" subfolder so index_*.html can reference "screenshots/<file>" relatively.
                String reportDirPath = ExtentStrategy.getReportDir();
                File screenshotDir;
                if (reportDirPath != null && !reportDirPath.isEmpty()) {
                    screenshotDir = new File(reportDirPath, "screenshots");
                } else {
                    screenshotDir = new File(System.getProperty("user.dir"), "target/extent-report/screenshots");
                }
                if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
                    log.warn("Could not create screenshot directory: {}", screenshotDir.getAbsolutePath());
                }

                String filename = (name == null || name.isEmpty()) ? "screenshot_" + System.currentTimeMillis() + ".png" : name;
                if (!filename.toLowerCase().endsWith(".png")) filename += ".png";
                File dest = new File(screenshotDir, filename);
                FileHandler.copy(src, dest);

                // Use relative path for Extent so images display inside the timestamped report folder.
                String pathForReport = (reportDirPath != null && !reportDirPath.isEmpty())
                        ? "screenshots/" + filename
                        : dest.getAbsolutePath();

                if (test != null) {
                    test.info("Screenshot: " + filename, MediaEntityBuilder.createScreenCaptureFromPath(pathForReport).build());
                } else {
                    log.info("Saved screenshot for extent: {}", dest.getAbsolutePath());
                }
                return;
            }
            log.debug("Extent: no driver or cannot take screenshot for {}", name);
        } catch (Throwable t) {
            log.warn("Extent attachScreenshot failed: {}", t.getMessage());
        }
    }
    @Override
    public void childStep(String name, Runnable runnable) {
        ExtentTest test = getCurrentTest();
        ExtentTest stepNode = null;
        if (test != null) {
            stepNode = test.createNode(name);
        }
        try {
            runnable.run();
            if (stepNode != null) {
                stepNode.pass("PASSED");
            }
        } catch (Throwable e) {
            if (stepNode != null) {
                stepNode.fail("FAILED: " + e.getMessage());
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }
    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        ExtentTest test = getCurrentTest();
        ExtentTest stepNode = null;
        if (test != null) {
            stepNode = test.createNode(name);
        }
        try {
            T result = supplier.get();
            if (stepNode != null) {
                stepNode.pass("PASSED");
            }
            return result;
        } catch (Throwable e) {
            if (stepNode != null) {
                stepNode.fail("FAILED: " + e.getMessage());
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }
}
