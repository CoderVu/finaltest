package org.example.core.assertion;

import java.util.function.Supplier;

public final class Expect {

    private Expect() {}

    public static <T> void equalsTo(Supplier<T> actualSupplier, T expected, String message) {
        T actual = actualSupplier.get();
        Assertions.get().assertEquals(actual, expected, message);
    }

    public static <T> void equalsTo(Supplier<T> actualSupplier, Supplier<T> expectedSupplier, String message) {
        T actual = actualSupplier.get();
        T expected = expectedSupplier.get();
        Assertions.get().assertEquals(actual, expected, message);
    }

    public static <T> void notEqualsTo(Supplier<T> actualSupplier, T expected, String message) {
        T actual = actualSupplier.get();
        Assertions.get().assertNotEquals(actual, expected, message);
    }

    public static void greaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        Assertions.get().assertTrue(actual > expected,
                message + " | Expected: > " + expected + ", Actual: " + actual);
    }

    public static void lessThan(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        Assertions.get().assertTrue(actual < expected,
                message + " | Expected: < " + expected + ", Actual: " + actual);
    }

    public static void greaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        Assertions.get().assertTrue(actual >= expected,
                message + " | Expected: >= " + expected + ", Actual: " + actual);
    }

    public static void lessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        int actual = actualSupplier.get();
        Assertions.get().assertTrue(actual <= expected,
                message + " | Expected: <= " + expected + ", Actual: " + actual);
    }
}
