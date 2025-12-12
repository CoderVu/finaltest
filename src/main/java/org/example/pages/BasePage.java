package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportClient;
import org.example.core.reporting.ReportingManager;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * - step(() -> { ... }) -  Create step using calling method name
 * - step("Step name", () -> { ... }) - Create step with custom name
 * - step(() -> { return value; }) - Create step and return value
 */
@Slf4j
public class BasePage {

    protected ReportClient reporter = ReportingManager.getReportClient();

    protected void step(Runnable action) {
        String methodName = getCallingMethodName();
        String stepName = formatMethodName(methodName);
        reporter.childStep(stepName, action);
    }

    /**
     * Create step with custom name.
     * 
     * Example:
     * step("Login with username: " + username, () -> {
     *     enterUsername(username);
     * });
     */
    protected void step(String stepName, Runnable action) {
        reporter.childStep(stepName, action);
    }

    /**
     * Create step and return value.
     * Automatically logs the return value to the report.
     */
    protected <T> T step(Supplier<T> supplier) {
        String methodName = getCallingMethodName();
        String stepName = formatMethodName(methodName);
        return reporter.childStep(stepName, () -> {
            T value = supplier.get();
            if (value != null) {
                reporter.info("Returned: " + formatReturnValue(value));
            }
            return value;
        });
    }

    /**
     * Create step with custom name and return value.
     * Automatically logs the return value to the report.
     */
    protected <T> T step(String stepName, Supplier<T> supplier) {
        return reporter.childStep(stepName, () -> {
            T value = supplier.get();
            if (value != null) {
                reporter.info("Returned: " + formatReturnValue(value));
            }
            return value;
        });
    }

    /**
     * Format return value for logging.
     * Handles primitives, objects, collections, arrays, etc.
     */
    private String formatReturnValue(Object value) {
        if (value == null) {
            return "null";
        }

        // Handle collections (List, Set, etc.)
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return "empty collection (size: 0)";
            }
            // Show first item if it's a small collection, or just size if large
            if (collection.size() <= 3) {
                return String.format("collection (size: %d): %s", collection.size(), collection);
            } else {
                Object first = collection.iterator().next();
                return String.format("collection (size: %d), first item: %s", 
                        collection.size(), formatReturnValue(first));
            }
        }

        // Handle arrays
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            if (length == 0) {
                return "empty array (length: 0)";
            }
            // Format array content
            StringBuilder sb = new StringBuilder();
            sb.append("array (length: ").append(length).append("): [");
            for (int i = 0; i < Math.min(length, 3); i++) {
                if (i > 0) sb.append(", ");
                sb.append(java.lang.reflect.Array.get(value, i));
            }
            if (length > 3) {
                sb.append(", ...");
            }
            sb.append("]");
            return sb.toString();
        }

        // For objects, use toString() which should be overridden (like Hotel.toString())
        // For primitives, toString() works fine
        return value.toString();
    }

    /**
     * log info
     */
    protected void logInfo(String message) {
        reporter.info(message);
    }

    /**
     * Attach screenshot.
     */
    protected void screenshot(String name) {
        reporter.attachScreenshot(name);
    }

    /**
     * Get method name of the caller of step()
     */
    private String getCallingMethodName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // stack[0] = getStackTrace
        // stack[1] = getCallingMethodName
        // stack[2] = step
        // stack[3] = method call step()
        if (stack.length > 3) {
            return stack[3].getMethodName();
        }
        return "unknown";
    }

    /**
     * Format following conventions:
     * ExampleMethodName -> "Example Method Name"
     */
    private String formatMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return "Unknown Step";
        }
        
        // Insert space before capital letters and capitalize first letter
        String formatted = methodName.replaceAll("([a-z])([A-Z])", "$1 $2");
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
