package org.example.core.testng.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.core.retry.NoRetry;
import org.example.core.retry.RetryContext;
import org.example.core.retry.RetryResult;
import org.example.core.retry.RetryService;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG adapter for RetryService.
 * Bridges TestNG IRetryAnalyzer interface to framework-agnostic RetryService.
 */
@Slf4j
public class TestNGRetryAnalyzer implements IRetryAnalyzer {

    private static final RetryService retryService = new RetryService();

    @Override
    public boolean retry(ITestResult result) {
        // Check if test has @NoRetry annotation
        if (result.getMethod() != null) {
            try {
                java.lang.reflect.Method method = result.getMethod().getConstructorOrMethod().getMethod();
                if (method != null && method.isAnnotationPresent(NoRetry.class)) {
                    return false;
                }
            } catch (Exception ignored) {
                // Ignore if cannot check annotation
            }
        }

        // Build retry context from TestNG result
        RetryContext context = buildRetryContext(result);

        // Use framework-agnostic retry service
        RetryResult retryResult = retryService.shouldRetry(context);

        // Store retry information in TestNG result attributes for reporting
        storeRetryInfo(result, retryResult);

        return retryResult.isShouldRetry();
    }

    /**
     * Builds RetryContext from TestNG ITestResult.
     */
    private RetryContext buildRetryContext(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testClass = result.getTestClass() != null ? result.getTestClass().getName() : null;

        // Extract browser from TestNG parameters
        String browser = null;
        try {
            String param = result.getTestContext()
                    .getCurrentXmlTest()
                    .getParameter("browser");
            if (param != null && !param.isBlank()) {
                browser = param;
            }
        } catch (Exception ignored) {
            // Ignore if cannot get browser parameter
        }

        return RetryContext.builder()
                .testName(testName)
                .testClass(testClass)
                .browser(browser)
                .frameworkContext(result) // Store TestNG context for potential future use
                .build();
    }

    /**
     * Stores retry information in TestNG result attributes for reporting.
     */
    private void storeRetryInfo(ITestResult result, RetryResult retryResult) {
        result.setAttribute("retry.currentAttempt", retryResult.getCurrentAttempt());
        result.setAttribute("retry.attempt", retryResult.getCurrentAttempt());
        result.setAttribute("retry.maxAttempts", retryResult.getMaxAttempts());
        result.setAttribute("retry.isRetry", retryResult.isRetry());
        result.setAttribute("retry.retryNumber", retryResult.getRetryNumber());
        result.setAttribute("retry.remainingAttempts", retryResult.getRemainingAttempts());
    }
}

