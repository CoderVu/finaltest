package org.example.report.impl;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.report.TestReporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extent Reporter - Implementation for ExtentReports.
 * Provides both standard TestReporter methods and Extent-specific features.
 * 
 * Features:
 * - Hierarchical test logs
 * - Screenshot support
 * - Rich HTML reports
 * - Advanced Extent-specific methods
 */
@Slf4j
public class ExtentReporter implements TestReporter {

    // Thread-safe storage for ExtentTest instances
    private static final Map<Long, ExtentTest> THREAD_TO_TEST = new ConcurrentHashMap<>();

    /**
     * Set ExtentTest for current thread (called by ExtentReportStrategy)
     */
    public static void setTest(ExtentTest test) {
        THREAD_TO_TEST.put(Thread.currentThread().getId(), test);
    }

    /**
     * Clear test for current thread
     */
    public static void clearTest() {
        THREAD_TO_TEST.remove(Thread.currentThread().getId());
    }

    private ExtentTest getCurrentTest() {
        return THREAD_TO_TEST.get(Thread.currentThread().getId());
    }

    // ==================== TestReporter Interface Methods ====================

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
    public void logInfo(String message) {
        info(message);
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
        if (test != null) {
            // Note: Screenshot attachment should be done via ExtentReportStrategy
            test.info("Screenshot: " + name);
        } else {
            log.info("Screenshot requested: {}", name);
        }
    }

    // ==================== Advanced Extent-Specific Methods ====================

    /**
     * Log a passed step
     */
    public void logPass(String message) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.pass(message);
        }
    }

    /**
     * Log a warning
     */
    public void logWarn(String message) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.warning(message);
        }
    }

    /**
     * Log with screenshot
     */
    public void logWithScreenshot(String message, String screenshotPath) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            try {
                test.info(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                log.warn("Failed to attach screenshot: {}", e.getMessage());
                test.info(message);
            }
        }
    }
}