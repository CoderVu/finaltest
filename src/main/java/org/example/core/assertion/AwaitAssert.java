package org.example.core.assertion;

import org.example.core.element.ISelElement;
import org.example.utils.DriverUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AwaitAssert {

    public static ElementExpectation expect(ISelElement element) {
        return new ElementExpectation(element);
    }

    public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
        Boolean actual = conditionSupplier.get();
        Assertions.get().assertEquals(actual, true, message + " | actual=" + actual + ", expected=true");
    }

    public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
        Boolean actual = conditionSupplier.get();
        Assertions.get().assertEquals(actual, false, message + " | actual=" + actual + ", expected=false");
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
        Expect.equalsTo(actualSupplier, expected, message);
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, Supplier<T> expectedSupplier, String message) {
        Expect.equalsTo(actualSupplier, expectedSupplier, message);
    }

    public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
        Expect.notEqualsTo(actualSupplier, expected, message);
    }

    public static void assertGreaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
        Expect.greaterThan(actualSupplier, expected, message);
    }

    public static void assertLessThan(Supplier<Integer> actualSupplier, int expected, String message) {
        Expect.lessThan(actualSupplier, expected, message);
    }

    public static void assertGreaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        Expect.greaterThanOrEqual(actualSupplier, expected, message);
    }

    public static void assertLessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
        Expect.lessThanOrEqual(actualSupplier, expected, message);
    }

    public static final class ElementExpectation {
        private static final Duration DEFAULT_TIMEOUT = DriverUtils.getTimeOut();
        private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(200);

        private final ISelElement element;
        private Duration timeout = DEFAULT_TIMEOUT;
        private Duration interval = DEFAULT_INTERVAL;
        private boolean negated = false;

        private ElementExpectation(ISelElement element) {
            this.element = element;
        }

        public ElementExpectation withTimeout(long timeoutMs) {
            this.timeout = Duration.ofMillis(Math.max(1, timeoutMs));
            return this;
        }

        public ElementExpectation withTimeout(Duration timeout) {
            this.timeout = (timeout == null || timeout.isNegative() || timeout.isZero())
                    ? DEFAULT_TIMEOUT
                    : timeout;
            return this;
        }

        public ElementExpectation withInterval(long intervalMs) {
            this.interval = Duration.ofMillis(Math.max(1, intervalMs));
            return this;
        }

        public ElementExpectation withInterval(Duration interval) {
            this.interval = (interval == null || interval.isNegative() || interval.isZero())
                    ? DEFAULT_INTERVAL
                    : interval;
            return this;
        }

        public ElementExpectation not() {
            this.negated = !this.negated;
            return this;
        }

        public void toBeVisible() {
            String message = expectationMessage("Element should be visible: " + element.getLocator());
            retryUntil(element::isVisible, Boolean.TRUE::equals, message, false);
        }

        public void toBeHidden() {
            String message = expectationMessage("Element should be hidden: " + element.getLocator());
            retryUntil(element::isVisible, actual -> Boolean.FALSE.equals(actual), message, false);
        }

        public void toBeEnabled() {
            String message = expectationMessage("Element should be enabled: " + element.getLocator());
            retryUntil(element::isEnabled, Boolean.TRUE::equals, message, false);
        }

        public void toBeDisabled() {
            String message = expectationMessage("Element should be disabled: " + element.getLocator());
            retryUntil(element::isEnabled, actual -> Boolean.FALSE.equals(actual), message, false);
        }

        public void toHaveText(String expected) {
            String message = expectationMessage("Element should have exact text '" + expected + "': " + element.getLocator());
            retryUntil(element::getText, actual -> expected != null && expected.equals(actual), message, true);
        }

        public void toContainText(String substring) {
            String message = expectationMessage("Element text should contain '" + substring + "': " + element.getLocator());
            retryUntil(element::getText, actual -> actual != null && substring != null && actual.contains(substring), message, true);
        }

        public void toHaveAttribute(String attribute, String expected) {
            String message = expectationMessage("Element attribute '" + attribute + "' should equal '" + expected + "': " + element.getLocator());
            retryUntil(() -> element.getAttribute(attribute), actual -> expected != null && expected.equals(actual), message, true);
        }

        public void toHaveValue(String expected) {
            String message = expectationMessage("Element value should equal '" + expected + "': " + element.getLocator());
            retryUntil(element::getValue, actual -> expected != null && expected.equals(actual), message, true);
        }

        private <T> void retryUntil(Supplier<T> actualSupplier, Predicate<T> passCondition, String message, boolean includeLastActual) {
            Instant deadline = Instant.now().plus(timeout);
            T lastActual = null;
            Throwable lastError = null;

            while (Instant.now().isBefore(deadline)) {
                try {
                    lastActual = actualSupplier.get();
                    boolean passed = passCondition.test(lastActual);
                    if (negated) {
                        passed = !passed;
                    }
                    if (passed) {
                        negated = false;
                        return;
                    }
                } catch (Throwable t) {
                    lastError = t;
                }
                sleep(interval);
            }

            String failMessage = includeLastActual ? message + " | lastActual=" + lastActual : message;
            if (lastError != null) {
                failMessage = failMessage + " | lastError=" + lastError.getClass().getSimpleName();
            }
            negated = false;
            Assertions.get().assertTrue(false, failMessage);
        }

        private String expectationMessage(String positiveMessage) {
            if (!negated) {
                return positiveMessage;
            }
            return "NOT(" + positiveMessage + ")";
        }

        private void sleep(Duration d) {
            try {
                Thread.sleep(Math.max(1, d.toMillis()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Assertion wait interrupted", e);
            }
        }
    }
}


