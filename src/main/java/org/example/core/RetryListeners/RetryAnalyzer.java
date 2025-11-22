package org.example.core.RetryListeners;

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

    @Override
    public boolean retry(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : "<unknown>";
        String fullTestName = testClass + "." + testName;
        
        AtomicInteger attemptCounter = TEST_ATTEMPTS.computeIfAbsent(fullTestName, k -> new AtomicInteger(0));
        
        int currentAttempt = attemptCounter.incrementAndGet();
            
        result.setAttribute(ATTEMPT_KEY, currentAttempt);
        result.setAttribute("retry.attempt", currentAttempt);
        
        log.info("[RETRY] Test FAILED - Attempt {}/{} for: {}",
                currentAttempt, maxAttempts, fullTestName);

        // If maxAttempts = 3, allow: attempt 1, 2, 3
        if (currentAttempt >= maxAttempts) {
            log.warn("[RETRY] Retry EXHAUSTED for {} after {} attempt(s). Test will be marked as FAILED.",
                    fullTestName, currentAttempt);
            TEST_ATTEMPTS.remove(fullTestName);
            return false;
        }
        log.info("[DRIVER] Quitting driver to ensure clean state for retry attempt {}", currentAttempt + 1);
        DriverFactory.quitDriver();
        return true;
    }
}
