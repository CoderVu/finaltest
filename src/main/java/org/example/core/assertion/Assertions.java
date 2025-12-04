package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.ReportClient;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
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

    public static Assertions get() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
        INSTANCE.set(new Assertions());
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



    /**
     * Handle assertion failure - report to reporting framework.
     */
    private void handleAssertionFailure(String message, Object expected, Object actual, AssertionError error) {
        String expectedStr = expected != null ? String.valueOf(expected) : "null";
        String actualStr = actual != null ? String.valueOf(actual) : "null";
        
        String stepName = "[" + getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "]: " + message +
                " | expected=" + expectedStr + " actual=" + actualStr;

        ReportClient client = ReportingManager.getReportClient();
        if (client != null) {
            client.logFail(stepName, error);
            try {
                client.attachScreenshot("assert_fail_" +  getCurrentTimestamp(DEFAULT_TIMESTAMP_REPORT_FORMAT) + ".png");
            } catch (Exception e) {
                log.debug("Unable to attach screenshot for assertion failure: {}", e.getMessage());
            }
        }
    }
}

