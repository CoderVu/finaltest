package org.example.core.assertion.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.assertion.Assertions;
import org.example.utils.DriverUtils;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class AwaitAssert {

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
        int attempt = 1;
        AssertionError lastError = null;

        do {
            if (attempt == attempts) {
                Assertions.setCurrentAttempt(attempt);
            } else {
                Assertions.setCurrentAttempt(null);
            }
            try {
                assertion.run();
                if (attempt > 1) {
                    log.info("Assertion passed on attempt {}/{}: {}", attempt, attempts, message);
                }
                Assertions.setCurrentAttempt(null);
                return;
            } catch (AssertionError error) {
                lastError = error;
                if (attempt < attempts) {
                    log.warn("Assertion failed on attempt {}/{}: {} - Retrying...", attempt, attempts, message);
                    DriverUtils.delay(DEFAULT_RETRY_DELAY.toMillis() / 1000.0);
                } else {
                    log.error("Assertion failed after {} attempts: {}", attempts, message);
                }
                attempt++;
            }
        } while (attempt <= attempts);

        if (lastError != null) {
            Assertions.setCurrentAttempt(null);
            throw lastError;
        }
    }
}


