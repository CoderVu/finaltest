package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;
import org.example.core.report.listener.IReportStrategyListener;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.util.*;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

@Slf4j
public class MySoftAssert extends SoftAssert {

    private static final ThreadLocal<MySoftAssert> SOFT = ThreadLocal.withInitial(MySoftAssert::new);
    private static final ThreadLocal<Boolean> ENABLE_AUTO_RETRY = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> RETRY_COUNT = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Runnable> PENDING_ASSERTION = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> RETRY_SUCCESS_MESSAGES = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Boolean> IS_RETRYING = ThreadLocal.withInitial(() -> false);

    public static MySoftAssert get() {
        return SOFT.get();
    }

    public static void setAutoRetryEnabled(boolean enabled) {
        ENABLE_AUTO_RETRY.set(enabled);
    }

    public static void setRetryCount(int count) {
        RETRY_COUNT.set(Math.max(0, count));
    }

    public static boolean isAutoRetryEnabled() {
        return ENABLE_AUTO_RETRY.get();
    }

    public static int getRetryCount() {
        return RETRY_COUNT.get();
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {
        PENDING_ASSERTION.remove();
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
                RETRY_SUCCESS_MESSAGES.get().add(message);
            } else {
                log.warn("Soft assertion failed even after auto-retry: {}", message);
            }
        }

        String stepName = "FAIL [" + getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "]: " + message +
                " | expected=" + expected + " actual=" + actual + (shouldRetry && !retrySucceeded ? " (retry failed)" : "");

        IReportStrategyListener strategy = ReportManager.selectStrategy();
        if (strategy != null && !retrySucceeded) {
            strategy.failStep(stepName);
        }

        org.testng.ITestResult current = org.testng.Reporter.getCurrentTestResult();
        if (current != null) {
            current.setAttribute("soft.assert.handled", Boolean.TRUE);
        }

        super.onAssertFailure(assertCommand, ex);
    }

    private boolean attemptRetry(IAssert<?> assertCommand, String message) {
        Runnable assertionLogic = PENDING_ASSERTION.get();
        if (assertionLogic == null) {
            return false;
        }

        boolean wasEnabled = ENABLE_AUTO_RETRY.get();
        int previousRetryCount = RETRY_COUNT.get();

        try {
            IS_RETRYING.set(true);
            ENABLE_AUTO_RETRY.set(false);
            RETRY_COUNT.set(0);

            int failuresBeforeRetry = getFailureCount();
            assertionLogic.run();
            int failuresAfterRetry = getFailureCount();
            boolean hasNewFailure = failuresAfterRetry > failuresBeforeRetry;

            if (hasNewFailure) {
                removeLastFailure();
                log.debug("Retry failed - new failure added and removed. Before: {}, After: {}",
                        failuresBeforeRetry, getFailureCount());
                return false;
            }

            PENDING_ASSERTION.remove();
            log.debug("Retry succeeded - no new failure added. Before: {}, After: {}",
                    failuresBeforeRetry, failuresAfterRetry);
            return true;
        } catch (AssertionError e) {
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during retry: {}", e.getMessage());
            return false;
        } finally {
            IS_RETRYING.set(false);
            ENABLE_AUTO_RETRY.set(wasEnabled);
            RETRY_COUNT.set(previousRetryCount);
        }
    }

    private int getFailureCount() throws NoSuchFieldException, IllegalAccessException {
        Field errorsField = SoftAssert.class.getDeclaredField("m_errors");
        errorsField.setAccessible(true);
        Object errorsObj = errorsField.get(this);

        if (errorsObj instanceof List) {
            return ((List<?>) errorsObj).size();
        } else if (errorsObj instanceof Map) {
            return ((Map<?, ?>) errorsObj).size();
        }
        return 0;
    }

    private void removeLastFailure() {
        try {
            Field errorsField = SoftAssert.class.getDeclaredField("m_errors");
            errorsField.setAccessible(true);
            Object errorsObj = errorsField.get(this);

            if (errorsObj instanceof List) {
                List<?> errors = (List<?>) errorsObj;
                if (!errors.isEmpty()) {
                    errors.remove(errors.size() - 1);
                }
            } else if (errorsObj instanceof Map) {
                Map<?, ?> errorsMap = (Map<?, ?>) errorsObj;
                if (!errorsMap.isEmpty()) {
                    Iterator<?> iterator = errorsMap.keySet().iterator();
                    Object lastKey = null;
                    while (iterator.hasNext()) {
                        lastKey = iterator.next();
                    }
                    if (lastKey != null) {
                        errorsMap.remove(lastKey);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not remove last failure: {}", e.getMessage());
        }
    }

    public static void setPendingAssertion(Runnable assertion) {
        PENDING_ASSERTION.set(assertion);
    }

    public static void clearPendingAssertion() {
        PENDING_ASSERTION.remove();
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
        SOFT.set(new MySoftAssert());
        ENABLE_AUTO_RETRY.set(false);
        RETRY_COUNT.set(0);
        PENDING_ASSERTION.remove();
        RETRY_SUCCESS_MESSAGES.remove();
        IS_RETRYING.set(false);
    }

    @Override
    public void assertAll() {
        java.util.Set<String> retrySuccessMessages = RETRY_SUCCESS_MESSAGES.get();
        if (!retrySuccessMessages.isEmpty()) {
            try {
                Field errorsField = SoftAssert.class.getDeclaredField("m_errors");
                errorsField.setAccessible(true);
                Object errorsObj = errorsField.get(this);

                if (errorsObj != null) {
                    int beforeSize = 0;
                    int removed = 0;

                    if (errorsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<IAssert<?>> errors = (List<IAssert<?>>) errorsObj;
                        beforeSize = errors.size();
                        errors.removeIf(assertCmd -> {
                            String msg = normalizeMessage(assertCmd.getMessage());
                            boolean shouldRemove = retrySuccessMessages.contains(msg);
                            if (shouldRemove) {
                                log.debug("Removing failure that passed after retry: {}", msg);
                            }
                            return shouldRemove;
                        });
                        removed = beforeSize - errors.size();
                    } else if (errorsObj instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        Map<?, IAssert<?>> errorsMap = (Map<?, IAssert<?>>) errorsObj;
                        beforeSize = errorsMap.size();
                        Iterator<?> iterator = errorsMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            @SuppressWarnings("unchecked")
                            Map.Entry<?, IAssert<?>> entry =
                                    (java.util.Map.Entry<?, IAssert<?>>) iterator.next();
                            IAssert<?> assertCmd = entry.getValue();
                            String msg = normalizeMessage(assertCmd.getMessage());
                            if (retrySuccessMessages.contains(msg)) {
                                log.debug("Removing failure that passed after retry: {}", msg);
                                iterator.remove();
                                removed++;
                            }
                        }
                    } else {
                        log.warn("Unexpected type for m_errors field: {}", errorsObj.getClass().getName());
                    }

                    if (removed > 0) {
                        log.info("Removed {} failure(s) that passed after retry", removed);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not remove retry success failures from SoftAssert: {}", e.getMessage());
            }
            retrySuccessMessages.clear();
        }

        super.assertAll();
    }
}
