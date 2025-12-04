package org.example.core.assertion.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.assertion.Assertions;
import org.example.core.element.util.DriverUtils;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class FunctionAssertions {

    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);

    public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
        assertBoolean(conditionSupplier, true, message);
    }

    public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
        assertBoolean(conditionSupplier, false, message);
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
        retryAssertion(message, () -> {
            T actual = actualSupplier.get();
            Assertions.get().assertEquals(actual, expected, message);
        });
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, Supplier<T> expectedSupplier, String message) {
        retryAssertion(message, () -> {
            T actual = actualSupplier.get();
            T expected = expectedSupplier.get();
            Assertions.get().assertEquals(actual, expected, message);
        });
    }

    public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
        retryAssertion(message, () -> {
            T actual = actualSupplier.get();
            Assertions.get().assertNotEquals(actual, expected, message);
        });
    }

    public static void assertGreaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
        retryAssertion(message, () -> {
            int actual = actualSupplier.get();
            Assertions.get().assertTrue(actual > expected,
                    message + " | Expected: > " + expected + ", Actual: " + actual);
        });
    }

    public static void assertLessThan(Supplier<Integer> actualSupplier, int expected, String message) {
        retryAssertion(message, () -> {
            int actual = actualSupplier.get();
            Assertions.get().assertTrue(actual < expected,
                    message + " | Expected: < " + expected + ", Actual: " + actual);
        });
    }

    public static void assertGreaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        retryAssertion(message, () -> {
            int actual = actualSupplier.get();
            Assertions.get().assertTrue(actual >= expected,
                    message + " | Expected: >= " + expected + ", Actual: " + actual);
        });
    }

    public static void assertLessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        retryAssertion(message, () -> {
            int actual = actualSupplier.get();
            Assertions.get().assertTrue(actual <= expected,
                    message + " | Expected: <= " + expected + ", Actual: " + actual);
        });
    }

    private static void assertBoolean(Supplier<Boolean> actualSupplier, boolean expected, String message) {
        retryAssertion(message, () -> {
            Boolean actual = actualSupplier.get();
            Assertions.get().assertEquals(actual, expected, message + " | actual=" + actual + ", expected=" + expected);
        });
    }

    private static void retryAssertion(String message, Runnable assertion) {
        int attempts = Math.max(1,
                Config.getIntPropertyOrDefault(Constants.MAX_NUM_OF_ATTEMPTS_SORTASSERT_PROPERTY, 1));
        AssertionError last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError error) {
                last = error;
                if (i < attempts) {
                    DriverUtils.delay(DEFAULT_RETRY_DELAY.toMillis() / 1000.0);
                }
            }
        }
        if (last != null) {
            throw last;
        }
    }
}


