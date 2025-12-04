package org.example.core.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.driver.factory.DriverFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core retry service - framework agnostic.
 * Can be used with TestNG, JUnit, or any other testing framework.
 */
@Slf4j
public class RetryService {

    private static final ConcurrentHashMap<String, AtomicInteger> TEST_ATTEMPTS = new ConcurrentHashMap<>();

    private final int maxAttempts;

    public RetryService() {
        this.maxAttempts = Math.max(1,
                Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_PROPERTY, 1));
    }

    public RetryService(int maxAttempts) {
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    /**
     * Determines if a test should be retried based on retry context.
     *
     * @param context Retry context containing test information
     * @return RetryResult with retry decision and attempt information
     */
    public RetryResult shouldRetry(RetryContext context) {
        String fullTestName = buildFullTestName(context);

        // Get or create attempt counter for this test
        AtomicInteger attemptCounter = TEST_ATTEMPTS.computeIfAbsent(fullTestName, k -> new AtomicInteger(0));

        // Increment counter: attempt 1 = first run, attempt 2 = first retry, etc.
        int currentAttempt = attemptCounter.incrementAndGet();

        // Calculate retry information
        boolean isRetry = (currentAttempt > 1);
        int retryNumber = Math.max(0, currentAttempt - 1);

        // Build retry result
        RetryResult result = RetryResult.builder()
                .shouldRetry(currentAttempt < maxAttempts)
                .currentAttempt(currentAttempt)
                .maxAttempts(maxAttempts)
                .isRetry(isRetry)
                .retryNumber(retryNumber)
                .remainingAttempts(Math.max(0, maxAttempts - currentAttempt))
                .fullTestName(fullTestName)
                .build();

        // Log retry information
        if (isRetry) {
            log.info("[RETRY] Test FAILED - Attempt {}/{} (Retry #{}) for: {}",
                    currentAttempt, maxAttempts, retryNumber, fullTestName);
        } else {
            log.info("[RETRY] Test FAILED - Attempt {}/{} (First attempt) for: {}",
                    currentAttempt, maxAttempts, fullTestName);
        }

        // Clean up counter if max attempts reached
        if (currentAttempt >= maxAttempts) {
            TEST_ATTEMPTS.remove(fullTestName);
        } else {
            // Quit driver before retry
            DriverFactory.quitDriver();
        }

        return result;
    }

    /**
     * Cleans up retry counter for a test.
     *
     * @param context Retry context
     */
    public void cleanup(RetryContext context) {
        String fullTestName = buildFullTestName(context);
        TEST_ATTEMPTS.remove(fullTestName);
    }

    /**
     * Builds a unique test name including browser to avoid conflicts when running tests in parallel.
     */
    private String buildFullTestName(RetryContext context) {
        String testName = context.getTestName();
        String testClass = context.getTestClass() != null ? context.getTestClass() : "<unknown>";
        String browser = context.getBrowser() != null && !context.getBrowser().isBlank()
                ? context.getBrowser() : "<browser>";

        return "[" + browser + "] " + testClass + "." + testName;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}




