package org.example.core.reporting.extent;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.reporting.ReportClient;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.listeners.CoreReportingListener;
import org.example.core.element.util.DriverUtils;
import org.example.core.retry.NoRetry;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExtentReportLifecycle implements CoreReportingListener {

    public static final String TEST_ATTRIBUTE = "reporting.extent.test";
    public static final String STEP_STACK_ATTRIBUTE = "reporting.extent.stepStack";
    public static final String ATTEMPT_NODE_ATTRIBUTE = "reporting.extent.attemptNode";

    // Single combined Extent report for all browsers / tests
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> TEST_ATTEMPT_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, ExtentTest> ATTEMPT_NODES = new ConcurrentHashMap<>(); // Track created attempt nodes: key = fullTestName + "_Attempt_" + attemptNumber
    private static final Map<String, Set<Integer>> TEST_ATTEMPT_NUMBERS = new ConcurrentHashMap<>(); // Track created attempt numbers per test
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Set<String> FAILED_TESTS = ConcurrentHashMap.newKeySet(); // Track failed tests for summary

    private static volatile String reportDir = null;

    public static String getReportDir() {
        return reportDir;
    }

    // =======================================================================
    // CoreReportingListener (engine-agnostic) API
    // These methods are invoked by engine-specific adapters such as
    // org.example.core.testng.listeners.TestNGReporter
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
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.warn("onTestStart(String) called but Reporter.getCurrentTestResult() is null for {}", testName);
            return;
        }
        onTestStart(result);
    }

    @Override
    public void onTestSuccess(String testName) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.warn("onTestSuccess(String) called but Reporter.getCurrentTestResult() is null for {}", testName);
            return;
        }
        onTestSuccess(result);
    }

    @Override
    public void onTestFailure(String testName, Throwable error) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.warn("onTestFailure(String, Throwable) called but Reporter.getCurrentTestResult() is null for {}", testName);
            // still log via ReportClient so failure is not lost
            ReportClient reporter = ReportingManager.getReportClient();
            reporter.logFail("Test failed: " + testName, error);
            return;
        }
        // Prefer the Throwable from TestNG result if available
        if (result.getThrowable() == null && error != null) {
            result.setThrowable(error);
        }
        onTestFailure(result);
    }

    @Override
    public void onTestSkipped(String testName) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.warn("onTestSkipped(String) called but Reporter.getCurrentTestResult() is null for {}", testName);
            return;
        }
        onTestSkipped(result);
    }

    @Override
    public void onConfigurationSuccess(String configName) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.debug("onConfigurationSuccess(String) called but Reporter.getCurrentTestResult() is null for {}", configName);
            return;
        }
        onConfigurationSuccess(result);
    }

    @Override
    public void onConfigurationFailure(String configName) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.debug("onConfigurationFailure(String) called but Reporter.getCurrentTestResult() is null for {}", configName);
            // fall back to generic logging
            ReportingManager.getReportClient().logFail("Config failed: " + configName, null);
            return;
        }
        onConfigurationFailure(result);
    }

    @Override
    public void onConfigurationSkip(String configName) {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            log.debug("onConfigurationSkip(String) called but Reporter.getCurrentTestResult() is null for {}", configName);
            return;
        }
        onConfigurationSkip(result);
    }

    // =======================================================================
    // Legacy TestNG-specific handlers (kept as helpers, now called via above)
    // =======================================================================

    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        // Add browser info into test name to distinguish runs
        String browser = "<browser>";
        try {
            String param = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");
            if (param != null && !param.isBlank()) {
                browser = param;
            }
        } catch (Exception ignored) {}
        
        // Add data index if available (for data-driven tests)
        // Note: We don't include data index in fullTestName to ensure all data sets of the same test
        // share the same attempt counter. Each data set will have its own test node but share attempts.
        String dataIndex = "";
        // Removed data index from fullTestName to fix counter issues
        // All data sets of the same test method will share the same attempt counter
        
        String fullTestName = "[" + browser + "] " + testClass + "." + testName + dataIndex;
        
        // Check if test has @NoRetry annotation - if so, don't create attempt nodes
        boolean hasNoRetry = false;
        try {
            if (result.getMethod() != null) {
                java.lang.reflect.Method method = result.getMethod().getConstructorOrMethod().getMethod();
                if (method != null) {
                    hasNoRetry = method.isAnnotationPresent(NoRetry.class);
                }
            }
        } catch (Exception ignored) {}
        
        // Get or create main test node
        ExtentTest test = NAME_TO_TEST.get(fullTestName);
        boolean isNewTest = (test == null);
        if (test == null) {
            test = EXTENT.createTest(fullTestName);
            NAME_TO_TEST.put(fullTestName, test);
        }
        
        // If @NoRetry annotation is present, skip attempt node creation and use test node directly
        if (hasNoRetry) {
            result.setAttribute(TEST_ATTRIBUTE, test);
            result.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
            log.info("[EXTENT] Starting test (NoRetry): {}", fullTestName);
            return;
        }
        
        // Get maxAttempts from config (same as RetryAnalyzer)
        int maxAttempts = Math.max(1, 
                Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
        
        // Check if attempt node already exists for this specific test result (to avoid duplicate nodes)
        ExtentTest existingAttemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        Integer existingAttempt = (Integer) result.getAttribute("retry.attempt");
        if (existingAttemptNode != null && existingAttempt != null) {
            // Attempt node already exists for this result - reuse it (this happens during retry)
            log.debug("Reusing existing attempt node for {} - Attempt {}", fullTestName, existingAttempt);
            return;
        }
        
        // Get attempt counter - reset ONLY if this is a completely new test (never seen before)
        AtomicInteger attemptCounter = TEST_ATTEMPT_COUNTERS.get(fullTestName);
        
        if (isNewTest) {
            // Completely new test - start fresh counter
            attemptCounter = new AtomicInteger(0);
            TEST_ATTEMPT_COUNTERS.put(fullTestName, attemptCounter);
            TEST_ATTEMPT_NUMBERS.put(fullTestName, ConcurrentHashMap.newKeySet());
            log.info("[EXTENT] Starting test: {} (maxAttempts: {})", fullTestName, maxAttempts);
        } else if (attemptCounter == null) {
            // Test node exists but counter was cleaned up (shouldn't happen, but handle gracefully)
            attemptCounter = new AtomicInteger(0);
            TEST_ATTEMPT_COUNTERS.put(fullTestName, attemptCounter);
            TEST_ATTEMPT_NUMBERS.put(fullTestName, ConcurrentHashMap.newKeySet());
            log.warn("[EXTENT] Test node exists but counter was missing for {} - resetting counter", fullTestName);
        }
        
        // Get set of attempt numbers already created for this test
        Set<Integer> createdAttempts = TEST_ATTEMPT_NUMBERS.computeIfAbsent(fullTestName, k -> ConcurrentHashMap.newKeySet());
        
        // Get current attempt number BEFORE incrementing
        int currentValue = attemptCounter.get();
        
        // Calculate next attempt number
        int nextAttemptNumber = currentValue + 1;
        
        // Safety check: if nextAttemptNumber would exceed maxAttempts, don't create new attempt
        if (nextAttemptNumber > maxAttempts) {
            log.warn("[EXTENT] Attempt counter for {} would exceed maxAttempts {}/{} - not creating new attempt.",
                    fullTestName, nextAttemptNumber, maxAttempts);
            // Try to find existing attempt node for maxAttempts
            String maxAttemptKey = fullTestName + "_Attempt_" + maxAttempts;
            ExtentTest maxAttemptNode = ATTEMPT_NODES.get(maxAttemptKey);
            if (maxAttemptNode != null) {
                result.setAttribute(ATTEMPT_NODE_ATTRIBUTE, maxAttemptNode);
                result.setAttribute(TEST_ATTRIBUTE, test);
                result.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
                result.setAttribute("retry.attempt", maxAttempts);
                log.debug("Reusing max attempt node for {} - Attempt {}", fullTestName, maxAttempts);
                return;
            }
            // If no existing node, use maxAttempts but don't increment counter
            result.setAttribute("retry.attempt", maxAttempts);
            ExtentTest attemptNode = test.createNode("Attempt " + maxAttempts);
            result.setAttribute(ATTEMPT_NODE_ATTRIBUTE, attemptNode);
            result.setAttribute(TEST_ATTRIBUTE, test);
            result.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
            attemptNode.info("Attempt " + maxAttempts + " execution started (counter limit reached)");
            return;
        }
        
        // Check if this attempt number was already created (to avoid duplicates)
        if (createdAttempts.contains(nextAttemptNumber)) {
            // Attempt node already created - reuse it and DON'T increment counter
            String attemptNodeKey = fullTestName + "_Attempt_" + nextAttemptNumber;
            ExtentTest cachedAttemptNode = ATTEMPT_NODES.get(attemptNodeKey);
            if (cachedAttemptNode != null) {
                result.setAttribute(ATTEMPT_NODE_ATTRIBUTE, cachedAttemptNode);
                result.setAttribute(TEST_ATTRIBUTE, test);
                result.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
                result.setAttribute("retry.attempt", nextAttemptNumber);
                log.debug("Reusing existing attempt node for {} - Attempt {}", fullTestName, nextAttemptNumber);
                return;
            }
        }
        
        // Only increment counter if this attempt number hasn't been created yet
        int attemptNumber = attemptCounter.incrementAndGet();
        
        // Double-check: if attemptNumber exceeds maxAttempts after increment, something went wrong
        if (attemptNumber > maxAttempts) {
            log.error("[EXTENT] Attempt number {} exceeds maxAttempts {} for {} - resetting to maxAttempts.",
                    attemptNumber, maxAttempts, fullTestName);
            attemptCounter.set(maxAttempts);
            attemptNumber = maxAttempts;
        }
        
        // Mark this attempt number as created
        createdAttempts.add(attemptNumber);
        
        // Store attempt number in result for reference
        result.setAttribute("retry.attempt", attemptNumber);
        
        log.info("[EXTENT] Test case: {} - Running Attempt {}/{}", fullTestName, attemptNumber, maxAttempts);
        
        // Create unique key for this attempt node
        String attemptNodeKey = fullTestName + "_Attempt_" + attemptNumber;
        
        // Create attempt node for better organization (test is guaranteed to be non-null here)
        ExtentTest attemptNode = test.createNode("Attempt " + attemptNumber);
        
        // Store attempt node in map to prevent duplicates
        ATTEMPT_NODES.put(attemptNodeKey, attemptNode);
        
        result.setAttribute(ATTEMPT_NODE_ATTRIBUTE, attemptNode);
        result.setAttribute(TEST_ATTRIBUTE, test);
        result.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
        
        // Don't log "Attempt X execution started" - let the steps speak for themselves
    }

    public void onTestSuccess(ITestResult result) {
        // Check if test actually passed - if there's a throwable, it's actually a failure
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            // Test has throwable but TestNG called onTestSuccess - this shouldn't happen, but handle it
            log.warn("onTestSuccess called but test has throwable: {} - treating as failure", throwable.getMessage());
            onTestFailure(result);
            return;
        }
        
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            // If there's an attempt node, ExtentReports will automatically set test status based on attempt node status
            // Don't manually set status here - let ExtentReports determine based on child nodes
        } else {
            getTest(result).ifPresent(t -> t.log(Status.PASS, "Test Passed"));
        }
        // Reset attempt counter when test passes
        cleanupAttemptCounter(result);
    }

    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        String message = getShortErrorMessage(error);

        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            // Ensure attempt node is marked as failed if it hasn't been already
            // The failed step may have already logged the failure, but we ensure status is set
            if (error != null) {
                attemptNode.fail(error);
            } else {
                attemptNode.fail(message);
            }
        } else {
            ExtentTest test = getTest(result).orElse(null);
            if (test != null) {
                test.log(Status.FAIL, error);
            }
            ReportClient reporter = ReportingManager.getReportClient();
            reporter.logFail(message, error);
        }
        
        // Track failed test for summary
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        FAILED_TESTS.add(fullTestName);
        
        // Check if retry is exhausted - if so, reset counter
        // RetryAnalyzer sets retry.attempt, and if it's >= maxAttempts, retry won't happen
        Integer retryAttempt = (Integer) result.getAttribute("retry.attempt");
        if (retryAttempt != null) {
            int maxAttempts = Math.max(1, 
                    Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
            if (retryAttempt >= maxAttempts) {
                // Retry exhausted, reset counter
                cleanupAttemptCounter(result);
            }
        }
    }

    public void onTestSkipped(ITestResult result) {
        // If test has a throwable (especially AssertionError from SoftAssert), 
        // it's actually a failure, not a skip. Let onTestFailure handle it.
        Throwable throwable = result.getThrowable();
        if (throwable != null && throwable instanceof AssertionError) {
            // This is actually a failure, not a skip. onTestFailure will handle it.
            log.debug("Test {} marked as skipped but has AssertionError - treating as failure", 
                    result.getMethod().getMethodName());
            // Don't log skip status or take screenshot here - let onTestFailure handle it
            // But still need to cleanup counter if retry exhausted
            Integer retryAttempt = (Integer) result.getAttribute("retry.attempt");
            if (retryAttempt != null) {
                int maxAttempts = Math.max(1, 
                        Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
                if (retryAttempt >= maxAttempts) {
                    cleanupAttemptCounter(result);
                }
            }
            return;
        }
        
        // This is a real skip (no throwable or non-assertion error)
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            attemptNode.log(Status.SKIP, throwable);
        } else {
            ExtentTest test = getTest(result).orElse(null);
            if (test != null) {
                test.log(Status.SKIP, throwable);
            }
        }
        
        // Only cleanup counter if retry is exhausted (don't cleanup on temporary skip during retry)
        Integer retryAttempt = (Integer) result.getAttribute("retry.attempt");
        if (retryAttempt != null) {
            int maxAttempts = Math.max(1, 
                    Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
            if (retryAttempt >= maxAttempts) {
                cleanupAttemptCounter(result);
            }
        } else {
            // No retry attempt info - assume retry exhausted or intentional skip
            cleanupAttemptCounter(result);
        }
    }

    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }

    public void onConfigurationSuccess(ITestResult itr) {
        ExtentTest test = getLastTestFromContext(itr);
        if (test != null) {
            itr.setAttribute(TEST_ATTRIBUTE, test);
            itr.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
            log.debug("Restored context for @Configuration method: {}", itr.getMethod().getMethodName());
        }
    }

    public void onConfigurationFailure(ITestResult itr) {
        Throwable t = itr.getThrowable();
        String message = (t == null) ? "Config failed" : "Config failed: " + t.getMessage();
        ReportClient reporter = ReportingManager.getReportClient();
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

    public void onConfigurationSkip(ITestResult itr) {
        log.debug("onConfigurationSkip called for {}", itr.getMethod().getMethodName());
    }

    private Optional<ExtentTest> getTest(ITestResult result) {
        ExtentTest test = (ExtentTest) result.getAttribute(TEST_ATTRIBUTE);
        if (test != null) {
            return Optional.of(test);
        }
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        return Optional.ofNullable(NAME_TO_TEST.get(fullTestName));
    }

    private ExtentTest getLastTestFromContext(ITestResult itr) {
        var ctx = itr.getTestContext();
        List<ITestResult> all = new ArrayList<>();
        all.addAll(ctx.getPassedTests().getAllResults());
        all.addAll(ctx.getFailedTests().getAllResults());
        all.addAll(ctx.getSkippedTests().getAllResults());
        if (all.isEmpty()) return null;

        all.sort(Comparator.comparingLong(ITestResult::getEndMillis).reversed());
        ITestResult lastResult = all.get(0);
        String lastTestName = lastResult.getMethod().getMethodName();
        String lastTestClass = lastResult.getTestClass() != null ? lastResult.getTestClass().getName() : "<unknown>";
        String lastFullTestName = lastTestClass + "." + lastTestName;
        return NAME_TO_TEST.get(lastFullTestName);
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

    private ExtentTest getCurrentExtentTest() {
        ITestResult current = Reporter.getCurrentTestResult();
        if (current == null) {
            return null;
        }
        ExtentTest test = (ExtentTest) current.getAttribute(TEST_ATTRIBUTE);
        if (test != null) {
            return test;
        }
        String testName = current.getMethod().getMethodName();
        String testClass = current.getTestClass() != null ? current.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        return NAME_TO_TEST.get(fullTestName);
    }

    /**
     * Builds full test name including browser (same logic as onTestStart).
     * Note: Data index is NOT included to ensure all data sets share the same attempt counter.
     */
    private String buildFullTestName(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String browser = "<browser>";
        try {
            String param = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");
            if (param != null && !param.isBlank()) {
                browser = param;
            }
        } catch (Exception ignored) {}
        
        // Don't include data index - all data sets share the same attempt counter
        return "[" + browser + "] " + testClass + "." + testName;
    }

    /**
     * Cleans up attempt counter for a test when it finishes (pass or retry exhausted).
     */
    private void cleanupAttemptCounter(ITestResult result) {
        String fullTestName = buildFullTestName(result);
        TEST_ATTEMPT_COUNTERS.remove(fullTestName);
        TEST_ATTEMPT_NUMBERS.remove(fullTestName);
        // Clean up attempt nodes for this test (remove all keys starting with fullTestName + "_Attempt_")
        ATTEMPT_NODES.entrySet().removeIf(entry -> entry.getKey().startsWith(fullTestName + "_Attempt_"));
        log.debug("[EXTENT] Cleaned up attempt counter, numbers, and nodes for: {}", fullTestName);
    }
}



