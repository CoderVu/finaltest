package org.example.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.core.assertion.MySoftAssert;

import java.util.function.Supplier;

@Slf4j
public class AssertionHelper {

    private static final int DEFAULT_RETRY_COUNT = 1;

    public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
        executeWithAutoRetry(
            () -> {
                boolean condition = conditionSupplier.get();
                MySoftAssert.get().assertTrue(condition, message);
            },
            message
        );
    }

    public static void assertTrue(boolean condition, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertTrue(condition, message),
            message
        );
    }

    public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
        executeWithAutoRetry(
            () -> {
                boolean condition = conditionSupplier.get();
                MySoftAssert.get().assertFalse(condition, message);
            },
            message
        );
    }

    public static void assertFalse(boolean condition, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertFalse(condition, message),
            message
        );
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
        executeWithAutoRetry(
            () -> {
                T actual = actualSupplier.get();
                MySoftAssert.get().assertEquals(actual, expected, message);
            },
            message
        );
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertEquals(actual, expected, message),
            message
        );
    }

    public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
        executeWithAutoRetry(
            () -> {
                T actual = actualSupplier.get();
                MySoftAssert.get().assertNotEquals(actual, expected, message);
            },
            message
        );
    }

    public static void assertNotEquals(Object actual, Object expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertNotEquals(actual, expected, message),
            message
        );
    }

    public static void assertGreaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
        executeWithAutoRetry(
            () -> {
                int actual = actualSupplier.get();
                MySoftAssert.get().assertTrue(actual > expected,
                    message + " | Expected: > " + expected + ", Actual: " + actual);
            },
            message
        );
    }

    public static void assertGreaterThan(int actual, int expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertTrue(actual > expected,
                message + " | Expected: > " + expected + ", Actual: " + actual),
            message
        );
    }

    public static void assertLessThan(Supplier<Integer> actualSupplier, int expected, String message) {
        executeWithAutoRetry(
            () -> {
                int actual = actualSupplier.get();
                MySoftAssert.get().assertTrue(actual < expected,
                    message + " | Expected: < " + expected + ", Actual: " + actual);
            },
            message
        );
    }

    public static void assertLessThan(int actual, int expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertTrue(actual < expected,
                message + " | Expected: < " + expected + ", Actual: " + actual),
            message
        );
    }

    public static void assertGreaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        executeWithAutoRetry(
            () -> {
                int actual = actualSupplier.get();
                MySoftAssert.get().assertTrue(actual >= expected,
                    message + " | Expected: >= " + expected + ", Actual: " + actual);
            },
            message
        );
    }

    public static void assertGreaterThanOrEqual(int actual, int expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertTrue(actual >= expected,
                message + " | Expected: >= " + expected + ", Actual: " + actual),
            message
        );
    }

    public static void assertLessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        executeWithAutoRetry(
            () -> {
                int actual = actualSupplier.get();
                MySoftAssert.get().assertTrue(actual <= expected,
                    message + " | Expected: <= " + expected + ", Actual: " + actual);
            },
            message
        );
    }

    public static void assertLessThanOrEqual(int actual, int expected, String message) {
        executeWithAutoRetry(
            () -> MySoftAssert.get().assertTrue(actual <= expected,
                message + " | Expected: <= " + expected + ", Actual: " + actual),
            message
        );
    }

    private static void executeWithAutoRetry(Runnable assertion, String message) {
        MySoftAssert.setPendingAssertion(assertion);
        boolean wasEnabled = MySoftAssert.isAutoRetryEnabled();
        int previousRetryCount = MySoftAssert.getRetryCount();
        
        try {
            if (!wasEnabled || previousRetryCount == 0) {
                MySoftAssert.setAutoRetryEnabled(true);
                MySoftAssert.setRetryCount(DEFAULT_RETRY_COUNT);
            }
            assertion.run();
        } finally {
            MySoftAssert.clearPendingAssertion();
        }
    }
}
