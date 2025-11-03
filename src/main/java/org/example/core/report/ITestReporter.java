package org.example.core.report;

public interface ITestReporter {

    void logStep(String message);
    void info(String message);

    default void logInfo(String message) {
        info(message);
    }
    void logFail(String message, Throwable error);
    void attachScreenshot(String name);

    // Main API for creating steps - must be implemented by reporters
    void childStep(String name, Runnable runnable);

    <T> T childStep(String name, java.util.function.Supplier<T> supplier);
}
