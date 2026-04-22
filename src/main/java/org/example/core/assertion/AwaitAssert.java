package org.example.core.assertion;

import org.example.core.element.ISelElement;
import org.example.utils.DriverUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.Reporter;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_REPORT_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

@Slf4j
public final class AwaitAssert {
    private static final ThreadLocal<List<AssertionError>> SOFT_FAILURES =
            ThreadLocal.withInitial(ArrayList::new);

    private AwaitAssert() {}

    public static ElementExpected expect(ISelElement element) {
        return new ElementExpected(element, false, false);
    }

    public static ElementExpected expectSoft(ISelElement element) {
        return new ElementExpected(element, false, true);
    }

    public static void assertAllSoft() {
        List<AssertionError> failures = SOFT_FAILURES.get();
        if (failures.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("Soft assertion failures (")
                .append(failures.size())
                .append("):");
        for (int i = 0; i < failures.size(); i++) {
            sb.append(System.lineSeparator())
                    .append(i + 1)
                    .append(") ")
                    .append(failures.get(i).getMessage());
        }

        SOFT_FAILURES.remove();
        throw new AssertionError(sb.toString());
    }

    public static void clearSoft() {
        SOFT_FAILURES.remove();
    }

    public static void assertTrue(BooleanSupplier condition, String message) {
        assertTrue(condition, message, DriverUtils.getTimeOut());
    }

    public static void assertTrue(BooleanSupplier condition, String message, Duration timeout) {
        pollUntil(
                condition::getAsBoolean,
                Boolean.TRUE::equals,
                normalizeTimeout(timeout),
                ElementExpected.DEFAULT_INTERVAL,
                message
        );
    }

    public static void assertFalse(BooleanSupplier condition, String message) {
        assertFalse(condition, message, DriverUtils.getTimeOut());
    }

    public static void assertFalse(BooleanSupplier condition, String message, Duration timeout) {
        assertTrue(() -> !condition.getAsBoolean(), message, timeout);
    }

    private static <T> void pollUntil(
            Supplier<T> supplier,
            Predicate<T> condition,
            Duration timeout,
            Duration interval,
            String message
    ) {
        Instant end = Instant.now().plus(timeout);
        T lastActual = null;
        int attempt = 0;

        log.info("AwaitAssert start: {} | timeout={}s | interval={}ms",
                message, timeout.toSeconds(), interval.toMillis());

        while (Instant.now().isBefore(end)) {
            attempt++;
            try {
                lastActual = supplier.get();
                log.debug("AwaitAssert attempt #{} | actual={}", attempt, lastActual);
                if (condition.test(lastActual)) {
                    log.info("AwaitAssert passed after {} attempt(s): {}", attempt, message);
                    return;
                }
            } catch (Exception e) {
                log.debug("AwaitAssert attempt #{} threw: {}", attempt, e.toString());
            }

            DriverUtils.delay(interval.toMillis() / 1000.0);
        }

        String finalMessage = message + " | lastActual=" + lastActual;
        log.error("AwaitAssert timeout after {} attempt(s): {}", attempt, finalMessage);
        throw buildFailure(finalMessage);
    }

    public static final class ElementExpected {

        private final ISelElement element;
        private final Duration timeout;
        private final Duration interval;
        private final boolean negated;
        private final boolean soft;

        static final Duration DEFAULT_TIMEOUT = DriverUtils.getTimeOut();
        static final Duration DEFAULT_INTERVAL = Duration.ofMillis(200);

        private ElementExpected(ISelElement element, boolean negated, boolean soft) {
            this(element, DEFAULT_TIMEOUT, DEFAULT_INTERVAL, negated, soft);
        }

        private ElementExpected(ISelElement element, Duration timeout, Duration interval, boolean negated, boolean soft) {
            this.element = element;
            this.timeout = timeout;
            this.interval = interval;
            this.negated = negated;
            this.soft = soft;
        }

        public ElementExpected withTimeout(long seconds) {
            return new ElementExpected(element, Duration.ofSeconds(seconds), interval, negated, soft);
        }

        public ElementExpected withTimeout(Duration timeout) {
            return new ElementExpected(element, normalizeTimeout(timeout), interval, negated, soft);
        }

        public ElementExpected withInterval(long ms) {
            return new ElementExpected(element, timeout, Duration.ofMillis(ms), negated, soft);
        }

        public ElementExpected withInterval(Duration interval) {
            return new ElementExpected(element, timeout, normalizeInterval(interval), negated, soft);
        }

        public ElementExpected not() {
            return new ElementExpected(element, timeout, interval, !negated, soft);
        }

        public ElementExpected soft() {
            return new ElementExpected(element, timeout, interval, negated, true);
        }

        public ElementExpected hard() {
            return new ElementExpected(element, timeout, interval, negated, false);
        }

        public boolean isVisible() {
            return checkUntil(
                    element::isVisible,
                    actual -> applyNegation(actual, true),
                    timeout,
                    interval
            );
        }

        public boolean isEnabled() {
            return checkUntil(
                    element::isEnabled,
                    actual -> applyNegation(actual, true),
                    timeout,
                    interval
            );
        }

        public void toBeVisible() {
            execute(
                    () -> pollUntil(
                            element::isVisible,
                            actual -> applyNegation(actual, true),
                            timeout,
                            interval,
                            buildMessage("Element should be visible")
                    )
            );
        }

        public void toBeHidden() {
            execute(
                    () -> pollUntil(
                            element::isVisible,
                            actual -> applyNegation(actual, false),
                            timeout,
                            interval,
                            buildMessage("Element should be hidden")
                    )
            );
        }

        public void toBeEnabled() {
            execute(
                    () -> pollUntil(
                            element::isEnabled,
                            actual -> applyNegation(actual, true),
                            timeout,
                            interval,
                            buildMessage("Element should be enabled")
                    )
            );
        }

        public void toHaveText(String expected) {
            String expectedNorm = normalize(expected);

            execute(
                    () -> pollUntil(
                            element::getText,
                            actual -> {
                                boolean match = Objects.equals(expectedNorm, normalize(actual));
                                return applyNegation(match, true);
                            },
                            timeout,
                            interval,
                            buildMessage("Expected text: " + expected)
                    )
            );
        }

        public void toContainText(String text) {
            String expectedNorm = normalize(text);

            execute(
                    () -> pollUntil(
                            element::getText,
                            actual -> {
                                String actualNorm = normalize(actual);
                                boolean contains = actualNorm != null && actualNorm.contains(expectedNorm);
                                return applyNegation(contains, true);
                            },
                            timeout,
                            interval,
                            buildMessage("Expected text contains: " + text)
                    )
            );
        }

        private void execute(Runnable action) {
            try {
                action.run();
            } catch (AssertionError e) {
                if (!soft) {
                    throw e;
                }
                SOFT_FAILURES.get().add(e);
                log.warn("Soft assertion captured: {}", e.getMessage());
            }
        }

        private boolean applyNegation(Boolean actual, boolean expectedTrue) {
            if (actual == null) {
                return false;
            }
            boolean result = expectedTrue ? actual : !actual;
            return negated ? !result : result;
        }

        private String normalize(String text) {
            if (text == null) {
                return null;
            }
            return text.trim().replaceAll("\\s+", " ");
        }

        private String buildMessage(String base) {
            return (negated ? "NOT(" : "")
                    + base
                    + " | locator="
                    + element.getLocator()
                    + (negated ? ")" : "");
        }

        private <T> boolean checkUntil(
                Supplier<T> supplier,
                Predicate<T> condition,
                Duration timeout,
                Duration interval
        ) {
            Instant end = Instant.now().plus(timeout);

            while (Instant.now().isBefore(end)) {
                try {
                    T actual = supplier.get();
                    if (condition.test(actual)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
                DriverUtils.delay(interval.toMillis() / 1000.0);
            }

            return false;
        }
    }

    private static Duration normalizeTimeout(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            return DriverUtils.getTimeOut();
        }
        return timeout;
    }

    private static Duration normalizeInterval(Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            return ElementExpected.DEFAULT_INTERVAL;
        }
        return interval;
    }

    private static AssertionError buildFailure(String finalMessage) {
        AssertionError failure = new AssertionError(finalMessage);
        Reporter reporter = ReportManager.getReporter();
        if (reporter != null) {
            reporter.logFail(finalMessage, failure);
            try {
                reporter.attachScreenshot("await_assert_fail_" + getCurrentTimestamp(DEFAULT_TIMESTAMP_REPORT_FORMAT) + ".png");
            } catch (Exception e) {
                log.debug("Unable to attach screenshot for await assertion failure: {}", e.getMessage());
            }
        }
        return failure;
    }
}
