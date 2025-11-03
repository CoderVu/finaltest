package org.example.core.report.strategy;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.DriverManager;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.enums.BrowserType;
import org.example.configure.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtentStrategy implements ReportStrategy {
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        File outDir = new File("target/extent");
        if (!outDir.exists()) outDir.mkdirs();
        ExtentSparkReporter spark = new ExtentSparkReporter(new File(outDir, "index.html"));
        EXTENT.attachReporter(spark);
        EXTENT.setSystemInfo("Suite", context.getSuite().getName());
        log.info("ExtentReports initialized at {}", outDir.getAbsolutePath());
    }
    @Override
    public void onFinish(ITestContext context) {
        try { EXTENT.flush(); } catch (Throwable t) { log.warn("Extent flush failed: {}", t.getMessage()); }
        ExtentTestReporter.clearTest();
    }
    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        ExtentTest test = EXTENT.createTest(name);
        NAME_TO_TEST.put(name, test);
        CURRENT_TEST.set(test);
        ExtentTestReporter.setTest(test);
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = getTest(result);
        if (test != null) test.log(Status.PASS, "Passed");
    }
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            if (org.example.core.report.FailureTracker.isHandledForCurrentThread()
                    || Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"))) {
                log.debug("Skipping Extent onTestFailure (already handled by soft-assert step): {}", result.getName());
                org.example.core.report.FailureTracker.clearForCurrentThread();
                return;
            }
        } catch (Throwable ignored) {}

        ExtentTest test = getTest(result);
        if (test != null) {
            Throwable t = result.getThrowable();
            String message = t == null ? "Failed" : t.getMessage();
            try {
                String screenshot = attachScreenshot();
                if (screenshot != null) {
                    test.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                } else {
                    test.log(Status.FAIL, message);
                }
            } catch (Exception e) {
                test.log(Status.FAIL, message);
            }
        }

        try { org.example.core.report.FailureTracker.clearForCurrentThread(); } catch (Throwable ignored) {}
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = getTest(result);
        if (test != null) {
            try {
                String screenshot = attachScreenshot();
                if (screenshot != null) {
                    test.skip("Skipped", MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                } else {
                    test.log(Status.SKIP, "Skipped");
                }
            } catch (Exception e) {
                test.log(Status.SKIP, "Skipped");
            }
        }
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }
    
    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        // For configuration methods (@AfterClass, @AfterMethod), ensure test context is available
        // so that @Step annotations in tearDown methods can log steps properly
        ExtentTest test = getTestForConfiguration(itr);
        if (test != null) {
            // Restore the test context so steps can be logged during tearDown
            CURRENT_TEST.set(test);
            ExtentTestReporter.setTest(test);
            log.debug("Restored test context for configuration method: {}", itr.getMethod().getMethodName());
        }
    }
    
    /**
     * Get the appropriate test for configuration methods.
     * For @AfterMethod: get the test that just completed (from ThreadLocal or test context)
     * For @AfterClass: get the last test that ran in the class
     */
    private ExtentTest getTestForConfiguration(ITestResult itr) {
        // First try to get from ThreadLocal (might still be set from the last test execution)
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            return test;
        }
        
        // Try to get from test result's test context
        try {
            if (itr.getTestContext() != null) {
                // Get all test results (passed, failed, skipped) to find the most recent one
                java.util.List<ITestResult> allResults = new java.util.ArrayList<>();
                
                // Add passed tests
                try {
                    java.util.Set<ITestResult> passedResults = itr.getTestContext().getPassedTests().getAllResults();
                    if (passedResults != null) {
                        allResults.addAll(passedResults);
                    }
                } catch (Throwable ignored) {}
                
                // Add failed tests
                try {
                    java.util.Set<ITestResult> failedResults = itr.getTestContext().getFailedTests().getAllResults();
                    if (failedResults != null) {
                        allResults.addAll(failedResults);
                    }
                } catch (Throwable ignored) {}
                
                // Add skipped tests
                try {
                    java.util.Set<ITestResult> skippedResults = itr.getTestContext().getSkippedTests().getAllResults();
                    if (skippedResults != null) {
                        allResults.addAll(skippedResults);
                    }
                } catch (Throwable ignored) {}
                
                // Sort by end time to get the most recent test
                if (!allResults.isEmpty()) {
                    allResults.sort((r1, r2) -> {
                        long time1 = r1.getEndMillis();
                        long time2 = r2.getEndMillis();
                        return Long.compare(time2, time1); // Most recent first
                    });
                    
                    // Get the most recent test
                    ITestResult mostRecentResult = allResults.get(0);
                    String testName = mostRecentResult.getMethod().getMethodName();
                    test = NAME_TO_TEST.get(testName);
                    if (test != null) {
                        return test;
                    }
                }
            }
        } catch (Throwable ignored) {
            // Fall through to return null
        }
        
        return null;
    }
    
    @Override
    public void onConfigurationFailure(ITestResult itr) {
        ExtentTest test = getTest(itr);
        if (test != null) {
            Throwable t = itr.getThrowable();
            String message = t == null ? "Configuration failed" : "CONFIG FAIL: " + t.getMessage();
            try {
                String screenshot = attachScreenshot();
                if (screenshot != null) {
                    test.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                } else {
                    test.log(Status.FAIL, message);
                }
            } catch (Exception e) {
                test.log(Status.FAIL, message);
            }
        }
    }
    
    @Override
    public void failStep(String name) {
        try {
            ExtentTest test = CURRENT_TEST.get();
            if (test == null) {
                // Create a transient test entry so the failure is visible immediately
                test = EXTENT.createTest(name);
            }
            String screenshotPath = attachScreenshot();
            if (screenshotPath != null) {
                test.fail(name, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } else {
                test.fail(name);
            }
        } catch (Throwable t) {
            log.debug("ExtentStrategy.failStep failed: {}", t.getMessage());
        }
    }

    private String attachScreenshot() {
        try {
            BrowserType browserType = Config.getBrowserType();
            WebDriver driver = DriverManager.getInstance(browserType).getDriver();
            if (driver != null && driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (screenshot != null && screenshot.length > 0) {
                    File screenshotDir = new File("target/extent/screenshots");
                    if (!screenshotDir.exists()) {
                        screenshotDir.mkdirs();
                    }
                    String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
                    File file = new File(screenshotDir, fileName);
                    java.nio.file.Files.write(file.toPath(), screenshot);
                    return file.getAbsolutePath();
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
    @Override public void onConfigurationSkip(ITestResult itr) { }
    private ExtentTest getTest(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) return test;
        return NAME_TO_TEST.get(result.getMethod().getMethodName());
    }
}
