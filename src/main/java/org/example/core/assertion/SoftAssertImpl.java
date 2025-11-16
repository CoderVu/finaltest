package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;
import org.example.core.report.listener.IReportStrategyListener;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

@Slf4j
public class SoftAssertImpl extends SoftAssert {

    private static final ThreadLocal<SoftAssertImpl> SOFT = ThreadLocal.withInitial(SoftAssertImpl::new);
    private static final ThreadLocal<Boolean> ENABLE_AUTO_RETRY = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> RETRY_COUNT = ThreadLocal.withInitial(() -> 0);

    public static SoftAssertImpl get() {
        return SOFT.get();
    }

    public static void setAutoRetryEnabled(boolean enabled) {
        ENABLE_AUTO_RETRY.set(enabled);
    }

    public static void setRetryCount(int count) {
        RETRY_COUNT.set(Math.max(0, count));
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = normalizeMessage(assertCommand.getMessage());
        if (message.isEmpty()) message = ex != null ? ex.getMessage() : "Assertion failed";

        String expected = stringify(assertCommand.getExpected());
        String actual = stringify(assertCommand.getActual());
        
        boolean shouldRetry = ENABLE_AUTO_RETRY.get() && RETRY_COUNT.get() > 0;
        boolean retrySucceeded = false;
        
        if (shouldRetry) {
            log.info("Soft assertion failed, attempting auto-retry ({} attempt(s) remaining): {}", 
                RETRY_COUNT.get(), message);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            retrySucceeded = attemptRetry(assertCommand, message);
            
            if (retrySucceeded) {
                log.info("Soft assertion passed after auto-retry: {}", message);
                IReporter reporter = ReportManager.getReporter();
                if (reporter != null) {
                    reporter.info("✓ Assertion passed after auto-retry: " + message);
                }
                return;
            } else {
                log.warn("Soft assertion failed even after auto-retry: {}", message);
            }
        }
        
        String stepName = "FAIL [" +  getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "]: " + message + 
            " | expected=" + expected + " actual=" + actual + (shouldRetry ? " (retry failed)" : "");

        IReportStrategyListener strategy = ReportManager.selectStrategy();
        if (strategy != null) {
            strategy.failStep(stepName);
        }

        org.testng.ITestResult current = org.testng.Reporter.getCurrentTestResult();
        if (current != null) {
            current.setAttribute("soft.assert.handled", Boolean.TRUE);
        }

        super.onAssertFailure(assertCommand, ex);
    }

    private boolean attemptRetry(IAssert<?> assertCommand, String message) {
        return false;
    }

    private String normalizeMessage(String message) {
        return message == null ? "" : message;
    }

    private String stringify(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (Exception e) {
            return "";
        }
    }

    public static void reset() {
        SOFT.remove();
        SOFT.set(new SoftAssertImpl());
        ENABLE_AUTO_RETRY.set(false);
        RETRY_COUNT.set(0);
    }

    @Override
    public void assertAll() {
        super.assertAll();
    }
}
