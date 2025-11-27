package org.example.core.testng.retryTCs;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.driver.factory.DriverFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final String ATTEMPT_KEY = "retry.currentAttempt";
    private static final ConcurrentHashMap<String, AtomicInteger> TEST_ATTEMPTS = new ConcurrentHashMap<>();

    private final int maxAttempts = Math.max(1,
            Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));

    /**
     * Builds a unique test name including browser to avoid conflicts when running tests in parallel.
     * This ensures each browser has its own retry counter.
     */
    private String buildFullTestName(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";

        // Add browser info to distinguish parallel runs
        String browser = "<browser>";
        try {
            String param = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");
            if (param != null && !param.isBlank()) {
                browser = param;
            }
        } catch (Exception ignored) {
        }

        return "[" + browser + "] " + testClass + "." + testName;
    }

    @Override
    public boolean retry(ITestResult result) {
        String fullTestName = buildFullTestName(result);

        // Get or create attempt counter for this test (per browser)
        AtomicInteger attemptCounter = TEST_ATTEMPTS.computeIfAbsent(fullTestName, k -> new AtomicInteger(0));

        // Increment counter: attempt 1 = first run, attempt 2 = first retry, etc.
        int currentAttempt = attemptCounter.incrementAndGet();

        // Calculate retry information
        boolean isRetry = (currentAttempt > 1);
        int retryNumber = Math.max(0, currentAttempt - 1); // Number of retries so far

        // Set comprehensive retry information for report to use
        result.setAttribute(ATTEMPT_KEY, currentAttempt);
        result.setAttribute("retry.attempt", currentAttempt);           // Current attempt number (1, 2, 3...)
        result.setAttribute("retry.maxAttempts", maxAttempts);          // Maximum allowed attempts
        result.setAttribute("retry.isRetry", isRetry);                    // Is this a retry? (true if attempt > 1)
        result.setAttribute("retry.retryNumber", retryNumber);          // Number of retries (0, 1, 2...)
        result.setAttribute("retry.remainingAttempts", Math.max(0, maxAttempts - currentAttempt)); // Remaining attempts

        if (isRetry) {
            log.info("[RETRY] Test FAILED - Attempt {}/{} (Retry #{}) for: {}",
                    currentAttempt, maxAttempts, retryNumber, fullTestName);
        } else {
            log.info("[RETRY] Test FAILED - Attempt {}/{} (First attempt) for: {}",
                    currentAttempt, maxAttempts, fullTestName);
        }

        // Check if we've reached max attempts
        // If maxAttempts = 2: attempt 1 (first) -> retry=true, attempt 2 (retry 1) -> retry=false
        if (currentAttempt >= maxAttempts) {

            // Clean up counter for this test
            TEST_ATTEMPTS.remove(fullTestName);
            return false; // No more retries
        }
        DriverFactory.quitDriver();
        return true; // Retry the test
    }
}
