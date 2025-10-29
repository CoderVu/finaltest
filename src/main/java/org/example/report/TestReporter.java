package org.example.report;

/**
 * Unified interface for reporting steps in Page Objects.
 * Implementations handle framework-specific logging.
 * 
 * Usage:
 * - logStep(): Log main test steps (e.g., "Login to application")
 * - info(): Log nested information or data within a step (e.g., "Username: admin", "Price: $99")
 * - logFail(): Log failures with error details
 * - attachScreenshot(): Attach screenshots
 */
public interface TestReporter {
    /**
     * Log a test step (main action).
     * This is the primary method for logging test actions.
     * Example: "Click login button", "Navigate to checkout page"
     */
    void logStep(String message);

    /**
     * Log informational message or data within a step.
     * Use this to log details, values, or nested information.
     * Example: "Username: john@example.com", "Total price: $150", "Found 5 items"
     */
    void info(String message);

    /**
     * Log informational message (alias for info method for backward compatibility).
     */
    default void logInfo(String message) {
        info(message);
    }

    /**
     * Log failure with error details
     */
    void logFail(String message, Throwable error);

    /**
     * Attach screenshot (optional)
     */
    default void attachScreenshot(String name) {
        // Default no-op, implementations can override
    }
}
