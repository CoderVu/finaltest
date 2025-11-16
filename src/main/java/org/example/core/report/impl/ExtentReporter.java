package org.example.core.report.impl;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.AbstractReporter;
import org.example.enums.ReportType;
import org.example.core.report.strategy.ExtentStrategyI;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.core.element.util.DriverUtils.getWebDriver;

/**
 * Implementation của IReporter cho Extent reporting.
 * Tương tự Firefox extends AbstractDriverManager trong package driver.
 */
@Slf4j
public class ExtentReporter extends AbstractReporter {
    private static final Map<Long, ExtentTest> THREAD_TO_TEST = new ConcurrentHashMap<>();
    private static final ThreadLocal<ExtentTest> CURRENT_STEP_NODE = new ThreadLocal<>();

    public ExtentReporter() {
        super(ReportType.EXTENT);
    }

    public static void setTest(ExtentTest test) {
        THREAD_TO_TEST.put(Thread.currentThread().getId(), test);
    }

    public static void clearTest() {
        THREAD_TO_TEST.remove(Thread.currentThread().getId());
        CURRENT_STEP_NODE.remove();
    }

    private ExtentTest getCurrentTest() {
        return THREAD_TO_TEST.get(Thread.currentThread().getId());
    }

    private ExtentTest getCurrentNode() {
        ExtentTest current = CURRENT_STEP_NODE.get();
        return current != null ? current : getCurrentTest();
    }

    @Override
    public boolean isInStep() {
        return CURRENT_STEP_NODE.get() != null;
    }

    @Override
    public void logStep(String message) {
        ExtentTest node = getCurrentNode();
        if (node != null) {
            node.info("STEP: " + message);
        }
    }

    @Override
    public void info(String message) {
        ExtentTest node = getCurrentNode();
        if (node != null) {
            node.info(message);
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
        WebDriver driver = getWebDriver();
        if (driver != null && (driver instanceof TakesScreenshot)) {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Prefer saving screenshots into the same timestamped report directory if available.
            // Save into a "screenshots" subfolder so index_*.html can reference "screenshots/<file>" relatively.
            String reportDirPath = ExtentStrategyI.getReportDir();
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
            try {
                FileHandler.copy(src, dest);
            } catch (Throwable e) {
                log.error("Failed to copy screenshot to destination: {}", e.getMessage());
            }

            // Use relative path for Extent so images display inside the timestamped report folder.
            String pathForReport = (reportDirPath != null && !reportDirPath.isEmpty())
                    ? "screenshots/" + filename
                    : dest.getAbsolutePath();

            if (test != null) {
                test.info("Screenshot: " + filename, MediaEntityBuilder.createScreenCaptureFromPath(pathForReport).build());
            }
        }
    }

    @Override
    public void childStep(String name, Runnable runnable) {
        ExtentTest parent = getCurrentNode();
        ExtentTest stepNode = null;
        ExtentTest previousNode = CURRENT_STEP_NODE.get();
        if (parent != null) {
            stepNode = parent.createNode(name);
            CURRENT_STEP_NODE.set(stepNode);
        }
        try {
            runnable.run();
            if (stepNode != null) {
                stepNode.pass("PASSED");
            }
        } catch (Throwable e) {
            if (stepNode != null) {
                attachScreenshotToNode(stepNode, "step_fail_" + name);
                stepNode.fail("FAILED: " + e.getMessage());
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            CURRENT_STEP_NODE.set(previousNode);
        }
    }

    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        ExtentTest parent = getCurrentNode();
        ExtentTest stepNode = null;
        ExtentTest previousNode = CURRENT_STEP_NODE.get();
        if (parent != null) {
            stepNode = parent.createNode(name);
            CURRENT_STEP_NODE.set(stepNode);
        }
        try {
            T result = supplier.get();
            if (stepNode != null) {
                stepNode.pass("PASSED");
            }
            return result;
        } catch (Throwable e) {
            if (stepNode != null) {
                attachScreenshotToNode(stepNode, "step_fail_" + name);
                stepNode.fail("FAILED: " + e.getMessage());
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        } finally {
            CURRENT_STEP_NODE.set(previousNode);
        }
    }

    public void attachScreenshotToNode(ExtentTest stepNode, String name) {
        WebDriver driver = getWebDriver();
        if (driver instanceof TakesScreenshot) {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String reportDirPath = ExtentStrategyI.getReportDir();
            File screenshotDir = (reportDirPath != null && !reportDirPath.isEmpty())
                    ? new File(reportDirPath, "screenshots")
                    : new File(System.getProperty("user.dir"), "target/extent-report/screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            String filename = (name == null || name.isEmpty()) ? "screenshot_" + System.currentTimeMillis() + ".png" : name;
            if (!filename.toLowerCase().endsWith(".png")) filename += ".png";
            try {
                FileHandler.copy(src, new File(screenshotDir, filename));
            } catch (Throwable e) {
                log.error("Failed to copy screenshot to destination: {}", e.getMessage());
            }
            String pathForReport = (reportDirPath != null && !reportDirPath.isEmpty())
                    ? "screenshots/" + filename
                    : new File(screenshotDir, filename).getAbsolutePath();
            stepNode.info("Screenshot", MediaEntityBuilder.createScreenCaptureFromPath(pathForReport).build());
        }
    }
}

