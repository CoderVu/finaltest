package org.example.core.report.impl;

import com.aventstack.extentreports.ExtentTest;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ITestReporter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        if (test != null) {
            test.info("Screenshot: " + name);
        } else {
            log.info("Screenshot requested: {}", name);
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
