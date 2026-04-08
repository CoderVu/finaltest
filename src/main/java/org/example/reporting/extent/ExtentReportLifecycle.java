package org.example.reporting.extent;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.context.TestContext;
import org.example.core.context.TestContextRegistry;
import org.example.core.reporting.Reporter;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.utils.DriverUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ExtentReportLifecycle implements ReportingListener {

    public static final String TEST_ATTRIBUTE = "reporting.extent.test";
    public static final String STEP_STACK_ATTRIBUTE = "reporting.extent.stepStack";

    // Single combined Extent report for all browsers / tests
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Set<String> FAILED_TESTS = ConcurrentHashMap.newKeySet(); // Track failed tests for summary

    private static volatile String reportDir = null;

    public static String getReportDir() {
        return reportDir;
    }

    // =======================================================================
    // ReportingListener (engine-agnostic) API
    // These methods are invoked by engine-specific adapters (TestNG, JUnit, etc.)
    // =======================================================================

    @Override
    public void onStart(String suiteName) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        String timestamp = new SimpleDateFormat(Constants.DEFAULT_TIMESTAMP_REPORT_FORMAT).format(new Date());
        reportDir = "target/extent-report/" + timestamp;
        File reportDirFile = new File(reportDir);
        if (!reportDirFile.exists() && !reportDirFile.mkdirs()) {
            log.warn("Could not create report directory: {}", reportDirFile.getAbsolutePath());
        }

        File htmlReport = new File(reportDir, "index_" + timestamp + ".html");
        ExtentSparkReporter spark = new ExtentSparkReporter(htmlReport.getAbsolutePath());

        spark.config().setDocumentTitle(suiteName);
        spark.config().setReportName(suiteName);
        spark.config().setEncoding("utf-8");

        EXTENT.attachReporter(spark);
        EXTENT.setSystemInfo("Suite", suiteName);
        EXTENT.setSystemInfo("Environment", Config.getEnvFile());
        EXTENT.setSystemInfo("Browsers", Config.getBrowserTypes().toString());
    }

    @Override
    public void onFinish(String suiteName) {
        EXTENT.flush();
    }

    @Override
    public void onTestStart(String testName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.warn("onTestStart(String) called but TestContext is null for {}", testName);
            return;
        }
        onTestStart(context, testName);
    }

    @Override
    public void onTestSuccess(String testName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.warn("onTestSuccess(String) called but TestContext is null for {}", testName);
            return;
        }
        onTestSuccess(context);
    }

    @Override
    public void onTestFailure(String testName, Throwable error) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.warn("onTestFailure(String, Throwable) called but TestContext is null for {}", testName);
            // still log via Reporter so failure is not lost
            Reporter reporter = ReportManager.getReporter();
            reporter.logFail("Test failed: " + testName, error);
            return;
        }
        onTestFailure(context, error);
    }

    @Override
    public void onTestSkipped(String testName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.warn("onTestSkipped(String) called but TestContext is null for {}", testName);
            return;
        }
        onTestSkipped(context);
    }

    @Override
    public void onConfigurationSuccess(String configName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.debug("onConfigurationSuccess(String) called but TestContext is null for {}", configName);
            return;
        }
        onConfigurationSuccess(context);
    }

    @Override
    public void onConfigurationFailure(String configName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.debug("onConfigurationFailure(String) called but TestContext is null for {}", configName);
            // fall back to generic logging
            ReportManager.getReporter().logFail("Config failed: " + configName, null);
            return;
        }
        onConfigurationFailure(context);
    }

    @Override
    public void onConfigurationSkip(String configName) {
        TestContext context = TestContextRegistry.getCurrentContext();
        if (context == null) {
            log.debug("onConfigurationSkip(String) called but TestContext is null for {}", configName);
            return;
        }
        onConfigurationSkip(context);
    }

    // =======================================================================
    // Framework-agnostic handlers (use TestContext instead of engine-specific types)
    // =======================================================================

    private void onTestStart(TestContext context, String testName) {
        // Get browser from context
        String browser = (String) context.getAttribute("test.browser");
        if (browser == null || browser.isBlank()) {
            browser = "<browser>";
        }

        String fullTestName = "[" + browser + "] " + buildDisplayTestName(testName);

        // Get or create main test node
        ExtentTest test = NAME_TO_TEST.get(fullTestName);
        if (test == null) {
            test = EXTENT.createTest(fullTestName);
            NAME_TO_TEST.put(fullTestName, test);
            log.info("[EXTENT] Starting test: {}", fullTestName);
        }

        context.setAttribute(TEST_ATTRIBUTE, test);
        context.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
    }

    private void onTestSuccess(TestContext context) {
        // Check if test actually passed - if there's a throwable, it's actually a failure
        Throwable throwable = (Throwable) context.getAttribute("test.throwable");
        if (throwable != null) {
            // Test has throwable but called onTestSuccess - this shouldn't happen, but handle it
            log.warn("onTestSuccess called but test has throwable: {} - treating as failure", throwable.getMessage());
            onTestFailure(context, throwable);
            return;
        }

        getTest(context).ifPresent(t -> t.log(Status.PASS, "Test Passed"));
    }

    private void onTestFailure(TestContext context, Throwable error) {
        ExtentTest test = getTest(context).orElse(null);
        String message = getShortErrorMessage(error);
        if (test != null) {
            test.log(Status.FAIL, error);
        }
        Reporter reporter = ReportManager.getReporter();
        reporter.logFail(message, error);

        String testName = (String) context.getAttribute("test.method");
        String testClass = (String) context.getAttribute("test.class");
        if (testClass == null) {
            testClass = "<unknown>";
        }
        String fullTestName = testClass + "." + testName;
        FAILED_TESTS.add(fullTestName);
    }

    private void onTestSkipped(TestContext context) {
        // If test has a throwable (especially AssertionError from SoftAssert),
        // it's actually a failure, not a skip. Let onTestFailure handle it.
        Throwable throwable = (Throwable) context.getAttribute("test.throwable");
        if (throwable != null && throwable instanceof AssertionError) {
            // This is actually a failure, not a skip. onTestFailure will handle it.
            String testName = (String) context.getAttribute("test.method");
            log.debug("Test {} marked as skipped but has AssertionError - treating as failure", testName);
            return;
        }

        // This is a real skip (no throwable or non-assertion error)
        ExtentTest test = getTest(context).orElse(null);
        if (test != null) {
            test.log(Status.SKIP, throwable);
        }
    }

    private void onConfigurationSuccess(TestContext context) {
        // For configuration methods, try to restore context from last test
        ExtentTest test = getLastTestFromContext();
        if (test != null) {
            context.setAttribute(TEST_ATTRIBUTE, test);
            context.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
            String configName = (String) context.getAttribute("test.method");
            log.debug("Restored context for configuration method: {}", configName);
        }
    }

    private void onConfigurationFailure(TestContext context) {
        Throwable t = (Throwable) context.getAttribute("test.throwable");
        String message = (t == null) ? "Config failed" : "Config failed: " + t.getMessage();
        Reporter reporter = ReportManager.getReporter();
        reporter.logFail(message, t);
        // Only attempt screenshot if driver session is valid
        try {
            WebDriver driver = DriverUtils.getWebDriver();
            if (driver != null) {
                reporter.attachScreenshot("config_failure_" + System.currentTimeMillis() + ".png");
            }
        } catch (Exception e) {
            log.debug("Skipping screenshot for configuration failure (driver unavailable): {}", e.getMessage());
        }
    }

    private void onConfigurationSkip(TestContext context) {
        String configName = (String) context.getAttribute("test.method");
        log.debug("onConfigurationSkip called for {}", configName);
    }

    private Optional<ExtentTest> getTest(TestContext context) {
        ExtentTest test = (ExtentTest) context.getAttribute(TEST_ATTRIBUTE);
        if (test != null) {
            return Optional.of(test);
        }
        String testName = (String) context.getAttribute("test.method");
        String testClass = (String) context.getAttribute("test.class");
        if (testClass == null) {
            testClass = "<unknown>";
        }
        String fullTestName = testClass + "." + testName;
        return Optional.ofNullable(NAME_TO_TEST.get(fullTestName));
    }

    private ExtentTest getLastTestFromContext() {
        if (NAME_TO_TEST.isEmpty()) {
            return null;
        }
        // Return the last test in the map (simple heuristic)
        return NAME_TO_TEST.values().stream()
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private String getShortErrorMessage(Throwable error) {
        if (error == null) return "Test Failed";
        String msg = error.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }

        if (msg.contains("The following asserts failed:")) {
            return "Soft assert failed";
        }

        String[] lines = msg.split("\n");
        return lines[0].trim();
    }

    private String buildDisplayTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "<unknown>";
        }
        return testName.trim();
    }
     
}

