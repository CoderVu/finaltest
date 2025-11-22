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
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.example.core.element.util.DriverUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExtentReportLifecycle implements ReportingLifecycleListener {

    public static final String TEST_ATTRIBUTE = "reporting.extent.test";
    public static final String STEP_STACK_ATTRIBUTE = "reporting.extent.stepStack";
    public static final String ATTEMPT_NODE_ATTRIBUTE = "reporting.extent.attemptNode";

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

    @Override
    public void onStart(ITestContext context) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        reportDir = "target/extent-report/" + timestamp;
        File reportDirFile = new File(reportDir);
        if (!reportDirFile.exists() && !reportDirFile.mkdirs()) {
            log.warn("Could not create report directory: {}", reportDirFile.getAbsolutePath());
        }

        File htmlReport = new File(reportDir, "index_" + timestamp + ".html");
        ExtentSparkReporter spark = new ExtentSparkReporter(htmlReport.getAbsolutePath());

        String suiteName = (context != null && context.getSuite() != null)
                ? context.getSuite().getName()
                : "Automation Test Suite";

        spark.config().setDocumentTitle(suiteName);
        spark.config().setReportName(suiteName);
        spark.config().setEncoding("utf-8");

        EXTENT.attachReporter(spark);
        EXTENT.setSystemInfo("Suite", suiteName);
        EXTENT.setSystemInfo("Environment", Config.getEnvFile());
        EXTENT.setSystemInfo("Browser", Config.getBrowserType().toString());
        log.info("ExtentReports initialized at {}", htmlReport.getAbsolutePath());
    }

    @Override
    public void onFinish(ITestContext context) {
        // Log test execution summary
        if (!FAILED_TESTS.isEmpty()) {
            StringBuilder summary = new StringBuilder("\n");
            summary.append("═══════════════════════════════════════════════════════════════\n");
            summary.append("                    TEST EXECUTION SUMMARY\n");
            summary.append("═══════════════════════════════════════════════════════════════\n");
            summary.append("Failed Tests (").append(FAILED_TESTS.size()).append("):\n");
            int index = 1;
            for (String failedTest : FAILED_TESTS) {
                summary.append("  ").append(index++).append(". ").append(failedTest).append("\n");
            }
            summary.append("═══════════════════════════════════════════════════════════════\n");
            log.info(summary.toString());
        } else {
            log.info("\n═══════════════════════════════════════════════════════════════\n" +
                    "                    TEST EXECUTION SUMMARY\n" +
                    "═══════════════════════════════════════════════════════════════\n" +
                    "All tests passed successfully!\n" +
                    "═══════════════════════════════════════════════════════════════\n");
        }
        
        EXTENT.flush();
        log.info("Extent report generated at {}", reportDir);
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        
        // Get maxAttempts from config (same as RetryAnalyzer)
        int maxAttempts = Math.max(1, 
                Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
        
        // Get or create main test node
        ExtentTest test = NAME_TO_TEST.get(fullTestName);
        boolean isNewTest = (test == null);
        if (test == null) {
            test = EXTENT.createTest(fullTestName);
            NAME_TO_TEST.put(fullTestName, test);
        }
        
        // Check if attempt node already exists (to avoid duplicate nodes if onTestStart is called multiple times)
        ExtentTest existingAttemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (existingAttemptNode != null) {
            // Attempt node already exists - reuse it
            Integer existingAttempt = (Integer) result.getAttribute("retry.attempt");
            if (existingAttempt != null) {
                log.debug("Reusing existing attempt node for {} - Attempt {}", fullTestName, existingAttempt);
                return;
            }
        }
        
        // Get attempt counter - reset ONLY if this is a new test or if counter was cleaned up
        AtomicInteger attemptCounter = TEST_ATTEMPT_COUNTERS.get(fullTestName);
        
        if (isNewTest || attemptCounter == null) {
            // New test or counter was cleaned up - start fresh
            attemptCounter = new AtomicInteger(0);
            TEST_ATTEMPT_COUNTERS.put(fullTestName, attemptCounter);
            log.info("🔄 [EXTENT] Starting test: {} (maxAttempts: {})", fullTestName, maxAttempts);
        }
        
        // Get current attempt number BEFORE incrementing
        int currentValue = attemptCounter.get();
        
        // Get set of attempt numbers already created for this test
        Set<Integer> createdAttempts = TEST_ATTEMPT_NUMBERS.computeIfAbsent(fullTestName, k -> ConcurrentHashMap.newKeySet());
        
        // Calculate next attempt number
        int nextAttemptNumber = currentValue + 1;
        
        // Safety check: if nextAttemptNumber would exceed maxAttempts, don't create new attempt
        if (nextAttemptNumber > maxAttempts) {
            log.warn("⚠ [EXTENT] Attempt counter for {} would exceed maxAttempts {}/{} - not creating new attempt.", 
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
        
        // Check if this attempt number was already created
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
            log.error("❌ [EXTENT] Attempt number {} exceeds maxAttempts {} for {} - resetting to maxAttempts.", 
                    attemptNumber, maxAttempts, fullTestName);
            attemptCounter.set(maxAttempts);
            attemptNumber = maxAttempts;
        }
        
        // Mark this attempt number as created
        createdAttempts.add(attemptNumber);
        
        // Store attempt number in result for reference
        result.setAttribute("retry.attempt", attemptNumber);
        
        log.info("📊 [EXTENT] Test case: {} - Running Attempt {}/{}", fullTestName, attemptNumber, maxAttempts);
        
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

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            // Don't set status here - ExtentReports will automatically set status based on steps inside
            // Only set status for main test node if no attempt node exists
        } else {
            getTest(result).ifPresent(t -> t.log(Status.PASS, "Test Passed"));
        }
        // Reset attempt counter when test passes
        cleanupAttemptCounter(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        String message = getShortErrorMessage(error);

        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
         // the failed step already logged the failure
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

    @Override
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
        // Reset attempt counter when test is skipped (implies retry exhausted or test was intentionally skipped)
        cleanupAttemptCounter(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.debug("onTestFailedButWithinSuccessPercentage called for {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        ExtentTest test = getLastTestFromContext(itr);
        if (test != null) {
            itr.setAttribute(TEST_ATTRIBUTE, test);
            itr.setAttribute(STEP_STACK_ATTRIBUTE, new ArrayDeque<ExtentTest>());
            log.debug("Restored context for @Configuration method: {}", itr.getMethod().getMethodName());
        }
    }

    @Override
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

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        log.debug("onConfigurationSkip called for {}", itr.getMethod().getMethodName());
    }

    @Override
    public void failStep(String stepName) {
        ITestResult current = Reporter.getCurrentTestResult();
        if (current == null) {
            ReportingManager.getReportClient().logFail(stepName, null);
            return;
        }
        
        // Get the active node (attempt node if available, otherwise test node)
        // This ensures the failed step is logged inside the attempt node
        ExtentTest activeNode = getActiveNodeForStep(current);
        if (activeNode != null) {
            ExtentTest stepNode = activeNode.createNode(stepName);
            ReportClient reporter = ReportingManager.getReportClient();
            if (reporter instanceof ExtentReportClient extentClient) {
                // Take screenshot for each failed step
                try {
                    WebDriver driver = DriverUtils.getWebDriver();
                    if (driver != null) {
                        extentClient.attachScreenshotToNode(stepNode, "softassert_" + System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    log.debug("Skipping screenshot for failed step (driver unavailable): {}", e.getMessage());
                }
            }
            stepNode.fail("FAILED");
        } else {
            ReportingManager.getReportClient().logFail(stepName, null);
        }
    }
    
    /**
     * Get the active node for logging steps.
     * Prioritizes attempt node over test node to ensure steps are logged inside attempts.
     */
    @SuppressWarnings("unchecked")
    private ExtentTest getActiveNodeForStep(ITestResult result) {
        if (result == null) {
            return null;
        }
        
        // Check if there's a step stack (nested steps)
        Deque<ExtentTest> stack = (Deque<ExtentTest>) result.getAttribute(STEP_STACK_ATTRIBUTE);
        if (stack != null && !stack.isEmpty()) {
            return stack.peek();
        }
        
        // Prioritize attempt node - this ensures steps are logged inside attempt nodes
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            return attemptNode;
        }
        
        // Fall back to main test node
        return getCurrentExtentTest();
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
     * Cleans up attempt counter for a test when it finishes (pass or retry exhausted).
     */
    private void cleanupAttemptCounter(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        TEST_ATTEMPT_COUNTERS.remove(fullTestName);
        TEST_ATTEMPT_NUMBERS.remove(fullTestName);
        // Clean up attempt nodes for this test (remove all keys starting with fullTestName + "_Attempt_")
        ATTEMPT_NODES.entrySet().removeIf(entry -> entry.getKey().startsWith(fullTestName + "_Attempt_"));
        log.debug("[EXTENT] Cleaned up attempt counter, numbers, and nodes for: {}", fullTestName);
    }
}



