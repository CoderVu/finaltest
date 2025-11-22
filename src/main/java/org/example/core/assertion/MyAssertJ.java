package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

/**
 * AssertJ-based assertion helper with reporting integration.
 * Assertions fail immediately (no soft assert behavior).
 * Uses AssertJ Exception Assertions for exception handling.
 */
@Slf4j
public class MyAssertJ {

    private static final ThreadLocal<MyAssertJ> INSTANCE = ThreadLocal.withInitial(MyAssertJ::new);

    public static MyAssertJ get() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
        INSTANCE.set(new MyAssertJ());
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
     * Assert that an object is null.
     * Fails immediately if object is not null.
     */
    public void assertNull(Object actual, String message) {
        try {
            assertThat(actual).as(message).isNull();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, actual, e);
            throw e;
        }
    }

    /**
     * Assert that an object is not null.
     * Fails immediately if object is null.
     */
    public void assertNotNull(Object actual, String message) {
        try {
            assertThat(actual).as(message).isNotNull();
        } catch (AssertionError e) {
            handleAssertionFailure(message, null, actual, e);
            throw e;
        }
    }

    /**
     * Assert that a runnable throws a specific exception.
     * Uses AssertJ Exception Assertions.
     * Fails immediately if exception is not thrown or wrong type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T assertThrows(Class<T> expectedException, Runnable runnable, String message) {
        Throwable thrown;
        try {
            runnable.run();
            // If we reach here, no exception was thrown
            AssertionError e = new AssertionError(message + " - Expected exception " + expectedException.getSimpleName() + " but no exception was thrown");
            handleAssertionFailure(message, expectedException.getSimpleName(), "No exception thrown", e);
            throw e;
        } catch (Throwable t) {
            thrown = t;
        }
        
        // Use AssertJ to verify exception type
        try {
            final Throwable finalThrown = thrown;
            assertThatThrownBy(() -> {
                throw finalThrown;
            })
            .as(message)
            .isInstanceOf(expectedException);
            
            return (T) thrown;
        } catch (AssertionError e) {
            handleAssertionFailure(message, expectedException.getSimpleName(), thrown.getClass().getSimpleName(), e);
            throw e;
        }
    }

    /**
     * Assert that a supplier throws a specific exception.
     * Uses AssertJ Exception Assertions.
     * Fails immediately if exception is not thrown or wrong type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T assertThrows(Class<T> expectedException, Supplier<?> supplier, String message) {
        Throwable thrown;
        try {
            supplier.get();
            // If we reach here, no exception was thrown
            AssertionError e = new AssertionError(message + " - Expected exception " + expectedException.getSimpleName() + " but no exception was thrown");
            handleAssertionFailure(message, expectedException.getSimpleName(), "No exception thrown", e);
            throw e;
        } catch (Throwable t) {
            thrown = t;
        }
        
        // Use AssertJ to verify exception type
        try {
            final Throwable finalThrown = thrown;
            assertThatThrownBy(() -> {
                throw finalThrown;
            })
            .as(message)
            .isInstanceOf(expectedException);
            
            return (T) thrown;
        } catch (AssertionError e) {
            handleAssertionFailure(message, expectedException.getSimpleName(), thrown.getClass().getSimpleName(), e);
            throw e;
        }
    }

    /**
     * Assert that a runnable does not throw any exception.
     * Uses AssertJ Exception Assertions.
     * Fails immediately if exception is thrown.
     */
    public void assertDoesNotThrow(Runnable runnable, String message) {
        try {
            assertThatThrownBy(runnable::run)
                    .as(message)
                    .doesNotThrowAnyException();
        } catch (AssertionError e) {
            handleAssertionFailure(message, "No exception", "Exception was thrown", e);
            throw e;
        }
    }

    /**
     * Assert that a supplier does not throw any exception and returns a value.
     * Uses AssertJ Exception Assertions.
     * Fails immediately if exception is thrown.
     */
    public <T> T assertDoesNotThrow(Supplier<T> supplier, String message) {
        try {
            T result = supplier.get();
            // Verify no exception was thrown using AssertJ
            assertThatThrownBy(() -> {})
                    .as(message)
                    .doesNotThrowAnyException();
            return result;
        } catch (Throwable t) {
            // If supplier throws, report it
            AssertionError e = new AssertionError(message + " - Expected no exception but got: " + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
            handleAssertionFailure(message, "No exception", t.getClass().getSimpleName() + ": " + t.getMessage(), e);
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

        ReportingLifecycleListener strategy = ReportingManager.getLifecycleListener();
        if (strategy != null) {
            strategy.failStep(stepName);
        }
    }
}

