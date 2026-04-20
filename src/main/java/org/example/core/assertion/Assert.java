package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.Reporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.common.Constants.DEFAULT_TIMESTAMP_REPORT_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

/**
 * Stateless assertion utility providing immediate assertions.
 * <p>
 * All assertions fail immediately (no soft assert behavior).
 * Integrates with ReportManager for failure reporting and screenshots.
 * Thread-safe as it contains no mutable state.
 */
@Slf4j
public final class Assert {

    private Assert() {}

    /**
     * Assert that a condition is true.
     * @param condition The condition to check
     * @param message   The assertion message
     * @throws AssertionError if condition is false
     */
    public static void assertTrue(boolean condition, String message) {
        try {
            assertThat(condition).as(message).isTrue();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, null, e);
            throw e;
        }
    }

    /**
     * Assert that a condition is false.
     * @param condition The condition to check
     * @param message   The assertion message
     * @throws AssertionError if condition is true
     */
    public static void assertFalse(boolean condition, String message) {
        try {
            assertThat(condition).as(message).isFalse();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, null, e);
            throw e;
        }
    }

    /**
     * Assert that two objects are equal.
     * @param actual    The actual value
     * @param expected  The expected value
     * @param message   The assertion message
     * @throws AssertionError if objects are not equal
     */
    public static void assertEquals(Object actual, Object expected, String message) {
        try {
            assertThat(actual).as(message).isEqualTo(expected);
        } catch (AssertionError e) {
            handleAssertionFailure(message, expected, actual, e);
            throw e;
        }
    }

    /**
     * Assert that two objects are not equal.
     * @param actual    The actual value
     * @param expected  The value to compare against
     * @param message   The assertion message
     * @throws AssertionError if objects are equal
     */
    public static void assertNotEquals(Object actual, Object expected, String message) {
        try {
            assertThat(actual).as(message).isNotEqualTo(expected);
        } catch (AssertionError e) {
            handleAssertionFailure(message, expected, actual, e);
            throw e;
        }
    }

    /**
     * Handle assertion failures by logging to report and capturing screenshot.
     * @param message       The assertion message
     * @param expected      The expected value (may be null)
     * @param actual        The actual value (may be null)
     * @param error         The AssertionError that occurred
     */
    private static void handleAssertionFailure(String message, Object expected, Object actual, AssertionError error) {
        String reportMessage = buildFailureMessage(message, expected, actual, error);

        Reporter reporter = ReportManager.getReporter();
        if (reporter != null) {
            reporter.logFail(reportMessage, error);
            try {
                reporter.attachScreenshot("assert_fail_" + getCurrentTimestamp(DEFAULT_TIMESTAMP_REPORT_FORMAT) + ".png");
            } catch (Exception e) {
                log.debug("Unable to attach screenshot for assertion failure: {}", e.getMessage());
            }
        }
    }

    /**
     * Build a detailed failure message combining assertion and error details.
     */
    private static String buildFailureMessage(String message, Object expected, Object actual, AssertionError error) {
        if (error.getMessage() != null) {
            return error.getMessage();
        }

        String expectedStr = expected != null ? String.valueOf(expected) : "null";
        String actualStr = actual != null ? String.valueOf(actual) : "null";
        return message + " | expected=" + expectedStr + " actual=" + actualStr;
    }
}

