package org.example.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.core.assertion.MyAssertJ;

import java.util.function.Supplier;

/**
 * Assertion helper using AssertJ-based assertions.
 * No retry mechanism - assertions fail immediately.
 */
@Slf4j
public class AssertionHelper {

    public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
        boolean condition = conditionSupplier.get();
        MyAssertJ.get().assertTrue(condition, message);
    }

    public static void assertTrue(boolean condition, String message) {
        MyAssertJ.get().assertTrue(condition, message);
    }

    public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
        boolean condition = conditionSupplier.get();
        MyAssertJ.get().assertFalse(condition, message);
    }

    public static void assertFalse(boolean condition, String message) {
        MyAssertJ.get().assertFalse(condition, message);
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
        T actual = actualSupplier.get();
        MyAssertJ.get().assertEquals(actual, expected, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        MyAssertJ.get().assertEquals(actual, expected, message);
    }

    public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
        T actual = actualSupplier.get();
        MyAssertJ.get().assertNotEquals(actual, expected, message);
    }

    public static void assertNotEquals(Object actual, Object expected, String message) {
        MyAssertJ.get().assertNotEquals(actual, expected, message);
    }

    public static void assertGreaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        MyAssertJ.get().assertTrue(actual > expected,
            message + " | Expected: > " + expected + ", Actual: " + actual);
    }

    public static void assertGreaterThan(int actual, int expected, String message) {
        MyAssertJ.get().assertTrue(actual > expected,
            message + " | Expected: > " + expected + ", Actual: " + actual);
    }

    public static void assertLessThan(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        MyAssertJ.get().assertTrue(actual < expected,
            message + " | Expected: < " + expected + ", Actual: " + actual);
    }

    public static void assertLessThan(int actual, int expected, String message) {
        MyAssertJ.get().assertTrue(actual < expected,
            message + " | Expected: < " + expected + ", Actual: " + actual);
    }

    public static void assertGreaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        MyAssertJ.get().assertTrue(actual >= expected,
            message + " | Expected: >= " + expected + ", Actual: " + actual);
    }

    public static void assertGreaterThanOrEqual(int actual, int expected, String message) {
        MyAssertJ.get().assertTrue(actual >= expected,
            message + " | Expected: >= " + expected + ", Actual: " + actual);
    }

    public static void assertLessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        MyAssertJ.get().assertTrue(actual <= expected,
            message + " | Expected: <= " + expected + ", Actual: " + actual);
    }

    public static void assertLessThanOrEqual(int actual, int expected, String message) {
        MyAssertJ.get().assertTrue(actual <= expected,
            message + " | Expected: <= " + expected + ", Actual: " + actual);
    }
}
