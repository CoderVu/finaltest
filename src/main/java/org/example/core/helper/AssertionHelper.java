package org.example.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.example.core.assertion.SoftAssertImpl;
import org.example.core.control.element.Element;
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class AssertionHelper {

    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    public static void assertTextEquals(Element element, String expectedText, String message) {
        assertWithRetry(
            () -> {
                String actualText = element.getText();
                SoftAssertImpl.get().assertEquals(actualText, expectedText, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                String actualText = element.getText();
                return actualText.equals(expectedText);
            },
            message + " (after retry)"
        );
    }

    public static void assertTextContains(Element element, String expectedSubstring, String message) {
        assertWithRetry(
            () -> {
                String actualText = element.getText();
                SoftAssertImpl.get().assertTrue(actualText.contains(expectedSubstring), 
                    message + " | Expected to contain: " + expectedSubstring + ", Actual: " + actualText);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                String actualText = element.getText();
                return actualText.contains(expectedSubstring);
            },
            message + " (after retry)"
        );
    }

    public static void assertElementVisible(Element element, String message) {
        assertWithRetry(
            () -> {
                boolean isVisible = element.isVisible();
                SoftAssertImpl.get().assertTrue(isVisible, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return element.isVisible();
            },
            message + " (after retry)"
        );
    }

    public static void assertElementEnabled(Element element, String message) {
        assertWithRetry(
            () -> {
                boolean isEnabled = element.isEnabled();
                SoftAssertImpl.get().assertTrue(isEnabled, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return element.isEnabled();
            },
            message + " (after retry)"
        );
    }

    public static void assertAttribute(Element element, String attributeName, String expectedValue, String message) {
        assertWithRetry(
            () -> {
                String actualValue = element.getAttribute(attributeName);
                SoftAssertImpl.get().assertEquals(actualValue, expectedValue, 
                    message + " | Attribute: " + attributeName);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                String actualValue = element.getAttribute(attributeName);
                return expectedValue.equals(actualValue);
            },
            message + " (after retry)"
        );
    }

    public static void assertAttributeContains(Element element, String attributeName, String expectedSubstring, String message) {
        assertWithRetry(
            () -> {
                String actualValue = element.getAttribute(attributeName);
                SoftAssertImpl.get().assertTrue(actualValue != null && actualValue.contains(expectedSubstring), 
                    message + " | Attribute: " + attributeName + " | Expected to contain: " + expectedSubstring + ", Actual: " + actualValue);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                String actualValue = element.getAttribute(attributeName);
                return actualValue != null && actualValue.contains(expectedSubstring);
            },
            message + " (after retry)"
        );
    }

    public static void assertHasClass(Element element, String expectedClass, String message) {
        assertAttributeContains(element, "class", expectedClass, message);
    }

    public static void assertElementSelected(Element element, String message) {
        assertWithRetry(
            () -> {
                boolean isSelected = element.isSelected();
                SoftAssertImpl.get().assertTrue(isSelected, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return element.isSelected();
            },
            message + " (after retry)"
        );
    }

    public static void assertValueEquals(Element element, String expectedValue, String message) {
        assertWithRetry(
            () -> {
                String actualValue = element.getValue();
                SoftAssertImpl.get().assertEquals(actualValue, expectedValue, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                String actualValue = element.getValue();
                return expectedValue.equals(actualValue);
            },
            message + " (after retry)"
        );
    }

    public static void assertTrue(boolean condition, String message) {
        if (condition) {
            SoftAssertImpl.get().assertTrue(true, message);
            return;
        }
        
        log.warn("Assertion failed, attempting retry: {}", message);
        
        try {
            Thread.sleep(RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        if (condition) {
            log.info("Assertion passed after retry: {}", message);
            IReporter reporter = ReportManager.getReporter();
            if (reporter != null) {
                reporter.info("✓ Assertion passed after retry: " + message);
            }
            SoftAssertImpl.get().assertTrue(true, message + " (after retry)");
        } else {
            log.error("Assertion failed even after retry: {}", message);
            SoftAssertImpl.get().assertTrue(false, message);
        }
    }

    public static void assertTrue(Supplier<Boolean> conditionSupplier, String message) {
        boolean condition = conditionSupplier.get();
        if (condition) {
            SoftAssertImpl.get().assertTrue(true, message);
            return;
        }
        
        log.warn("Assertion failed, attempting retry: {}", message);
        
        try {
            Thread.sleep(RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        boolean retryCondition = conditionSupplier.get();
        if (retryCondition) {
            log.info("Assertion passed after retry: {}", message);
            IReporter reporter = ReportManager.getReporter();
            if (reporter != null) {
                reporter.info("✓ Assertion passed after retry: " + message);
            }
            SoftAssertImpl.get().assertTrue(true, message + " (after retry)");
        } else {
            log.error("Assertion failed even after retry: {}", message);
            SoftAssertImpl.get().assertTrue(false, message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertFalse(condition, message),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return !condition;
            },
            message + " (after retry)"
        );
    }

    public static void assertFalse(Supplier<Boolean> conditionSupplier, String message) {
        boolean condition = conditionSupplier.get();
        if (!condition) {
            SoftAssertImpl.get().assertFalse(false, message);
            return;
        }
        
        log.warn("Assertion failed, attempting retry: {}", message);
        
        try {
            Thread.sleep(RETRY_DELAY.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        boolean retryCondition = conditionSupplier.get();
        if (!retryCondition) {
            log.info("Assertion passed after retry: {}", message);
            IReporter reporter = ReportManager.getReporter();
            if (reporter != null) {
                reporter.info("✓ Assertion passed after retry: " + message);
            }
            SoftAssertImpl.get().assertFalse(false, message + " (after retry)");
        } else {
            log.error("Assertion failed even after retry: {}", message);
            SoftAssertImpl.get().assertFalse(true, message);
        }
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertEquals(actual, expected, message),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if (actual == null && expected == null) return true;
                if (actual == null || expected == null) return false;
                return actual.equals(expected);
            },
            message + " (after retry)"
        );
    }

    public static <T> void assertEquals(Supplier<T> actualSupplier, T expected, String message) {
        assertWithRetry(
            () -> {
                T actual = actualSupplier.get();
                SoftAssertImpl.get().assertEquals(actual, expected, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                T actual = actualSupplier.get();
                if (actual == null && expected == null) return true;
                if (actual == null || expected == null) return false;
                return actual.equals(expected);
            },
            message + " (after retry)"
        );
    }

    public static void assertNotEquals(Object actual, Object expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertNotEquals(actual, expected, message),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if (actual == null && expected == null) return false;
                if (actual == null || expected == null) return true;
                return !actual.equals(expected);
            },
            message + " (after retry)"
        );
    }

    public static <T> void assertNotEquals(Supplier<T> actualSupplier, T expected, String message) {
        assertWithRetry(
            () -> {
                T actual = actualSupplier.get();
                SoftAssertImpl.get().assertNotEquals(actual, expected, message);
            },
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                T actual = actualSupplier.get();
                if (actual == null && expected == null) return false;
                if (actual == null || expected == null) return true;
                return !actual.equals(expected);
            },
            message + " (after retry)"
        );
    }

    public static void assertGreaterThan(int actual, int expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertTrue(actual > expected, 
                message + " | Expected: > " + expected + ", Actual: " + actual),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return actual > expected;
            },
            message + " (after retry)"
        );
    }

    public static void assertLessThan(int actual, int expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertTrue(actual < expected, 
                message + " | Expected: < " + expected + ", Actual: " + actual),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return actual < expected;
            },
            message + " (after retry)"
        );
    }

    public static void assertGreaterThanOrEqual(int actual, int expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertTrue(actual >= expected, 
                message + " | Expected: >= " + expected + ", Actual: " + actual),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return actual >= expected;
            },
            message + " (after retry)"
        );
    }

    public static void assertLessThanOrEqual(int actual, int expected, String message) {
        assertWithRetry(
            () -> SoftAssertImpl.get().assertTrue(actual <= expected, 
                message + " | Expected: <= " + expected + ", Actual: " + actual),
            () -> {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return actual <= expected;
            },
            message + " (after retry)"
        );
    }

    private static void assertWithRetry(Runnable assertion, Supplier<Boolean> retryCheck, String retryMessage) {
        try {
            assertion.run();
        } catch (AssertionError e) {
            log.warn("Assertion failed, attempting retry: {}", e.getMessage());
            
            try {
                boolean retrySuccess = retryCheck.get();
                if (retrySuccess) {
                    log.info("Assertion passed after retry: {}", retryMessage);
                    IReporter reporter = ReportManager.getReporter();
                    if (reporter != null) {
                        reporter.info("✓ Assertion passed after retry: " + retryMessage);
                    }
                } else {
                    log.error("Assertion failed even after retry");
                    throw e;
                }
            } catch (Exception retryEx) {
                log.error("Retry check failed: {}", retryEx.getMessage());
                throw e;
            }
        }
    }

    public static void assertWithRetry(Runnable assertion, int retryCount, Duration delay) {
        int attempts = 0;
        AssertionError lastError = null;
        
        while (attempts <= retryCount) {
            try {
                assertion.run();
                if (attempts > 0) {
                    log.info("Assertion passed after {} retry attempts", attempts);
                }
                return;
            } catch (AssertionError e) {
                lastError = e;
                attempts++;
                if (attempts <= retryCount) {
                    log.warn("Assertion failed (attempt {}/{}), retrying...", attempts, retryCount + 1);
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        
        if (lastError != null) {
            throw lastError;
        }
    }
}
