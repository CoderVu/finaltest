package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.ReportClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.common.Constants.DEFAULT_TIMESTAMP_REPORT_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

/**
 * AssertJ-based assertion helper with reporting integration.
 * Assertions fail immediately (no soft assert behavior).
 * Uses AssertJ Exception Assertions for exception handling.
 */
@Slf4j
public class Assertions {

    private static final ThreadLocal<Assertions> INSTANCE = ThreadLocal.withInitial(Assertions::new);
    private static final ThreadLocal<Integer> CURRENT_ATTEMPT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ASSERTION_LOGGED = new ThreadLocal<>();

    public static Assertions get() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
        INSTANCE.set(new Assertions());
        CURRENT_ATTEMPT.remove();
        ASSERTION_LOGGED.remove();
    }

    /**
     * Sets the current attempt number for retry assertions.
     * Used by FunctionAssertions to track retry attempts.
     * Pass null to clear.
     */
    public static void setCurrentAttempt(Integer attempt) {
        if (attempt == null) {
            CURRENT_ATTEMPT.remove();
        } else {
            CURRENT_ATTEMPT.set(attempt);
        }
    }

    private static Integer getCurrentAttempt() {
        return CURRENT_ATTEMPT.get();
    }

    public static boolean isAssertionLogged() {
        return Boolean.TRUE.equals(ASSERTION_LOGGED.get());
    }

    public static void markAssertionLogged() {
        ASSERTION_LOGGED.set(true);
    }

    /**
     * Assert that a condition is true.
     * Fails immediately if condition is false.
     */
    public void assertTrue(boolean condition, String message) {
        try {
            assertThat(condition).as(message).isTrue();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, null, e);
            throw e;
        }
    }

    /**
     * Assert that a condition is false.
     * Fails immediately if condition is true.
     */
    public void assertFalse(boolean condition, String message) {
        try {
            assertThat(condition).as(message).isFalse();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, null, e);
            throw e;
        }
    }

    /**
     * Assert that two objects are equal.
     * Fails immediately if objects are not equal.
     */
    public void assertEquals(Object actual, Object expected, String message) {
        try {
            assertThat(actual).as(message).isEqualTo(expected);
        } catch (AssertionError e) {
            handleAssertionFailure(message, expected, actual, e);
            throw e;
        }
    }

    /**
     * Assert that two objects are not equal.
     * Fails immediately if objects are equal.
     */
    public void assertNotEquals(Object actual, Object expected, String message) {
        try {
            assertThat(actual).as(message).isNotEqualTo(expected);
        } catch (AssertionError e) {
            handleAssertionFailure(message, expected, actual, e);
            throw e;
        }
    }



    private void handleAssertionFailure(String message, Object expected, Object actual, AssertionError error) {
        Integer attempt = getCurrentAttempt();
        
        if (attempt == null) {
            return;
        }
        
        String attemptInfo = (attempt > 1) ? " [Attempt " + attempt + "]" : "";
        
        String reportMessage;
        if (error.getMessage() != null) {
            reportMessage = error.getMessage() + attemptInfo;
        } else {
            String expectedStr = expected != null ? String.valueOf(expected) : "null";
            String actualStr = actual != null ? String.valueOf(actual) : "null";
            reportMessage = message + attemptInfo + " | expected=" + expectedStr + " actual=" + actualStr;
        }

        ReportClient client = ReportingManager.getReportClient();
        if (client != null) {
            client.logFail(reportMessage, error);
            try {
                client.attachScreenshot("assert_fail_" +  getCurrentTimestamp(DEFAULT_TIMESTAMP_REPORT_FORMAT) + ".png");
            } catch (Exception e) {
                log.debug("Unable to attach screenshot for assertion failure: {}", e.getMessage());
            }
            markAssertionLogged();
        }
    }
}

