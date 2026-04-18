package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.ISelElement;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.Reporter;
import org.example.utils.DateUtils;
import org.example.utils.DriverUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_REPORT_FORMAT;

/**
 * Provides await-based assertions with automatic retry logic.
 * <p>
 * All assertions support configurable timeout, polling interval, and retry tracking.
 * Failures include comprehensive debug information: timeout, attempts, last actual value.
 * <p>
 * Thread-safe: creates immutable ElementExpected instances for each expect() call.
 */
@Slf4j
public final class AwaitAssert {

    private AwaitAssert() {}

        /**
         * Create an expectation for an element with retry support.
         * @param element The ISelElement to assert on
         * @return An immutable ElementExpected instance
         */
        public static ElementExpected expect(ISelElement element) {
            return new ElementExpected(element, false);
        }

        /**
         * Assert a boolean condition resolves to true within timeout.
         * @param conditionSupplier Supplies the condition to check
         * @param message           The assertion message
         * @throws AssertionError if condition doesn't resolve to true within timeout
         */
        public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
            retryUntil(conditionSupplier, Boolean.TRUE::equals, message, true);
        }

        /**
         * Assert a boolean condition resolves to false within timeout.
         * @param conditionSupplier Supplies the condition to check
         * @param message           The assertion message
         * @throws AssertionError if condition doesn't resolve to false within timeout
         */
        public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
            retryUntil(conditionSupplier, value -> Boolean.FALSE.equals(value), message, true);
        }

        /**
         * Assert that a supplier value equals expected within timeout.
         * @param actualSupplier Supplies the actual value
         * @param expected       The expected value
         * @param message        The assertion message
         * @throws AssertionError if values don't match within timeout
         */
        public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
            retryUntil(actualSupplier, actual -> expected != null && expected.equals(actual), message, true);
        }

        /**
         * Assert that two supplier values equal each other within timeout.
         * @param actualSupplier    Supplies the actual value
         * @param expectedSupplier  Supplies the expected value
         * @param message           The assertion message
         * @throws AssertionError if values don't match within timeout
         */
        public static <T> void assertEquals(Supplier<T> actualSupplier, Supplier<T> expectedSupplier, String message) {
            retryUntil(actualSupplier, actual -> {
                T expected = expectedSupplier.get();
                return actual != null && actual.equals(expected);
            }, message, true);
        }

        /**
         * Assert that a supplier value does NOT equal expected within timeout.
         * @param actualSupplier Supplies the actual value
         * @param expected       The value to compare against
         * @param message        The assertion message
         * @throws AssertionError if value matches expected within timeout
         */
        public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
            retryUntil(actualSupplier, actual -> !(expected != null && expected.equals(actual)), message, true);
        }

        /**
         * Assert that a numeric value is greater than threshold within timeout.
         * @param actualSupplier Supplies the numeric value
         * @param expected       The threshold
         * @param message        The assertion message
         * @throws AssertionError if value is not greater than threshold within timeout
         */
        public static void assertGreaterThan(Supplier<Integer> actualSupplier, int expected, String message) {
            retryUntil(actualSupplier, actual -> actual != null && actual > expected,
                    message + " | Expected: > " + expected, true);
        }

        /**
         * Assert that a numeric value is less than threshold within timeout.
         * @param actualSupplier Supplies the numeric value
         * @param expected       The threshold
         * @param message        The assertion message
         * @throws AssertionError if value is not less than threshold within timeout
         */
        public static void assertLessThan(Supplier<Integer> actualSupplier, int expected, String message) {
            retryUntil(actualSupplier, actual -> actual != null && actual < expected,
                    message + " | Expected: < " + expected, true);
        }

        /**
         * Assert that a numeric value is greater than or equal to threshold within timeout.
         * @param actualSupplier Supplies the numeric value
         * @param expected       The threshold
         * @param message        The assertion message
         * @throws AssertionError if value is not >= threshold within timeout
         */
        public static void assertGreaterThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
            retryUntil(actualSupplier, actual -> actual != null && actual >= expected,
                    message + " | Expected: >= " + expected, true);
        }

        /**
         * Assert that a numeric value is less than or equal to threshold within timeout.
         * @param actualSupplier Supplies the numeric value
         * @param expected       The threshold
         * @param message        The assertion message
         * @throws AssertionError if value is not <= threshold within timeout
         */
        public static void assertLessThanOrEqual(Supplier<Integer> actualSupplier, int expected, String message) {
            retryUntil(actualSupplier, actual -> actual != null && actual <= expected,
                    message + " | Expected: <= " + expected, true);
        }

        /**
         * Core retry logic: polls a supplier until predicate passes or timeout.
         * <p>
         * Tracks: attempts, elapsed time, last actual value, last error.
         * Reports comprehensive failure messages with all debug info.
         *
         * @param supplier        Provides the value to test
         * @param passCondition   Predicate that returns true to pass assertion
         * @param message         The assertion message
         * @param includeActual   Whether to include last actual value in failure message
         * @throws AssertionError if condition doesn't pass within default timeout
         */
        private static <T> void retryUntil(Supplier<T> supplier, Predicate<T> passCondition,
                                            String message, boolean includeActual) {
            retryUntil(supplier, passCondition, message, includeActual,
                    DriverUtils.getTimeOut(), Duration.ofMillis(200));
        }

        /**
         * Core retry logic with custom timeout and interval.
         *
         * @param supplier        Provides the value to test
         * @param passCondition   Predicate that returns true to pass assertion
         * @param message         The assertion message
         * @param includeActual   Whether to include last actual value in failure message
         * @param timeout         Maximum time to retry
         * @param interval        Time between retry attempts
         * @throws AssertionError if condition doesn't pass within timeout
         */
        private static <T> void retryUntil(Supplier<T> supplier, Predicate<T> passCondition,
                                            String message, boolean includeActual,
                                            Duration timeout, Duration maxInterval) {
            Instant startTime = Instant.now();
            Instant deadline = startTime.plus(timeout);
            T lastActual = null;
            Throwable lastError = null;
            int attempts = 0;

            while (Instant.now().isBefore(deadline)) {
                attempts++;
                try {
                    lastActual = supplier.get();
                    if (passCondition.test(lastActual)) {
                        log.trace("Assertion passed after {} attempts", attempts);
                        return;
                    }
                } catch (Throwable t) {
                    lastError = t;
                    log.trace("Retry due to exception: {}", t.getClass().getSimpleName());
                }

                if (Instant.now().isBefore(deadline)) {
                    adaptiveSleep(attempts, maxInterval);
                }
            }

            throw buildAssertionError(message, lastActual, lastError, attempts, timeout, includeActual);
        }

        /**
         * Build a comprehensive AssertionError for retry failures.
         */
        private static AssertionError buildAssertionError(
                String message,
                Object lastActual,
                Throwable lastError,
                int attempts,
                Duration timeout,
                boolean includeActual) {
            StringBuilder sb = new StringBuilder(message);

            if (includeActual) {
                sb.append(" | lastActual=").append(lastActual);
            }

            sb.append(" | attempts=").append(attempts);
            sb.append(", timeout=").append(timeout.toMillis()).append("ms");
            if (lastError != null) {
                sb.append(", lastError=").append(lastError.getClass().getSimpleName());
            }

            String finalMessage = sb.toString();
            Reporter reporter = ReportManager.getReporter();
            if (reporter != null) {
                reporter.logFail(finalMessage);
                try {
                    reporter.attachScreenshot("await_assert_fail_" + DateUtils.getCurrentTimestamp(DEFAULT_TIMESTAMP_REPORT_FORMAT) + ".png");
                } catch (Exception ignored) {
                    log.debug("Unable to attach screenshot for await assertion failure: {}", ignored.getMessage());
                }
            }

            return new AssertionError(finalMessage);
        }

        /**
         * Adaptive sleep similar to Playwright polling.
         */
        private static void adaptiveSleep(int attempt, Duration maxInterval) {
            try {
                long delay = Math.min(maxInterval.toMillis(), 20L * attempt);
                Thread.sleep(Math.max(1, delay));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Assertion wait interrupted", e);
            }
        }

        /**
         * Immutable expectation builder for element-based assertions.
         * <p>
         * Supports fluent API: expect(element).withTimeout(...).not().toBeVisible()
         * <p>
         * NOT mutating - not() returns a new instance with negation set.
         * <p>
         * All terminal methods integrate with ReportManager for reporting.
         */
        public static final class ElementExpected {
            private static final Duration DEFAULT_TIMEOUT = DriverUtils.getTimeOut();
            private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(200);

            private final ISelElement element;
            private final Duration timeout;
            private final Duration interval;
            private final boolean negated;

            private ElementExpected(ISelElement element, boolean negated) {
                this(element, DEFAULT_TIMEOUT, DEFAULT_INTERVAL, negated);
            }

            private ElementExpected(ISelElement element, Duration timeout, Duration interval, boolean negated) {
                this.element = element;
                this.timeout = timeout == null || timeout.isNegative() || timeout.isZero()
                        ? DEFAULT_TIMEOUT : timeout;
                this.interval = interval == null || interval.isNegative() || interval.isZero()
                        ? DEFAULT_INTERVAL : interval;
                this.negated = negated;
            }

            /**
             * Set custom timeout for this expectation (immutable operation).
             * @param timeoutMs Timeout in milliseconds
             * @return New ElementExpected with updated timeout
             */
            public ElementExpected withTimeout(long timeoutMs) {
                return new ElementExpected(element, Duration.ofMillis(Math.max(1, timeoutMs)), interval, negated);
            }

            /**
             * Set custom timeout for this expectation (immutable operation).
             * @param timeout Timeout duration
             * @return New ElementExpected with updated timeout
             */
            public ElementExpected withTimeout(Duration timeout) {
                return new ElementExpected(element, timeout, interval, negated);
            }

            /**
             * Set custom polling interval for this expectation (immutable operation).
             * @param intervalMs Interval in milliseconds
             * @return New ElementExpected with updated interval
             */
            public ElementExpected withInterval(long intervalMs) {
                return new ElementExpected(element, timeout, Duration.ofMillis(Math.max(1, intervalMs)), negated);
            }

            /**
             * Set custom polling interval for this expectation (immutable operation).
             * @param interval Interval duration
             * @return New ElementExpected with updated interval
             */
            public ElementExpected withInterval(Duration interval) {
                return new ElementExpected(element, timeout, interval, negated);
            }

            /**
             * Negate the next assertion (immutable operation).
             * @return New ElementExpected with negation toggled
             */
            public ElementExpected not() {
                return new ElementExpected(element, timeout, interval, !negated);
            }

            /**
             * Assert element is visible.
             */
            public void toBeVisible() {
                String message = expectationMessage("Element should be visible: " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.isVisible(), this::testCondition, message, false, timeout, interval));
            }

            /**
             * Assert element is hidden.
             */
            public void toBeHidden() {
                String message = expectationMessage("Element should be hidden: " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.isVisible(), actual -> testCondition(actual, true), message, false, timeout, interval));
            }

            /**
             * Assert element is enabled.
             */
            public void toBeEnabled() {
                String message = expectationMessage("Element should be enabled: " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.isEnabled(), this::testCondition, message, false, timeout, interval));
            }

            /**
             * Assert element is disabled.
             */
            public void toBeDisabled() {
                String message = expectationMessage("Element should be disabled: " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.isEnabled(), actual -> testCondition(actual, true), message, false, timeout, interval));
            }

            /**
             * Assert element has exact text.
             */
            public void toHaveText(String expected) {
                String message = expectationMessage("Element should have exact text '" + expected + "': " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.getText(), actual -> {
                            boolean match = expected != null && expected.equals(actual);
                            return negated ? !match : match;
                        }, message, true, timeout, interval));
            }

            /**
             * Assert element text contains substring.
             */
            public void toContainText(String substring) {
                String message = expectationMessage("Element text should contain '" + substring + "': " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.getText(),
                                actual -> {
                                    boolean contains = actual != null && substring != null && actual.contains(substring);
                                    return negated ? !contains : contains;
                                },
                                message, true, timeout, interval));
            }

            /**
             * Assert element attribute equals expected value.
             */
            public void toHaveAttribute(String attribute, String expected) {
                String message = expectationMessage("Element attribute '" + attribute + "' should equal '" + expected + "': " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.getAttribute(attribute),
                                actual -> {
                                    boolean match = expected != null && expected.equals(actual);
                                    return negated ? !match : match;
                                },
                                message, true, timeout, interval));
            }

            /**
             * Assert element value equals expected value.
             */
            public void toHaveValue(String expected) {
                String message = expectationMessage("Element value should equal '" + expected + "': " + element.getLocator());
                executeExpectationStep(message, () ->
                        retryUntil(() -> element.getValue(),
                                actual -> {
                                    boolean matches = expected != null && expected.equals(actual);
                                    return negated ? !matches : matches;
                                },
                                message, true, timeout, interval));
            }

            /**
             * Execute assertion step with optional reporting.
             */
            private void executeExpectationStep(String stepName, Runnable action) {
                Reporter reporter = ReportManager.getReporter();
                if (reporter == null) {
                    action.run();
                    return;
                }

                if (reporter.isInStep()) {
                    action.run();
                    return;
                }

                reporter.childStep(stepName, () -> {
                    action.run();
                    reporter.info("PASSED");
                });
            }

            /**
             * Format expectation message with negation if needed.
             */
            private String expectationMessage(String positiveMessage) {
                return negated ? "NOT(" + positiveMessage + ")" : positiveMessage;
            }

            /**
             * Test a boolean condition with negation applied.
             */
            private boolean testCondition(Boolean value) {
                return testCondition(value, false);
            }

            /**
             * Test a boolean condition with optional inversion and negation applied.
             */
            private boolean testCondition(Boolean value, boolean invert) {
                if (value == null) return false;
                boolean result = invert ? !value : value;
                return negated ? !result : result;
            }
        }
    }


