package org.example.core.report;

public interface TestReporter {

    void logStep(String message);
    void info(String message);

    default void logInfo(String message) {
        info(message);
    }
    void logFail(String message, Throwable error);
    void attachScreenshot(String name);

    // Reporter-agnostic step wrappers
    default void withinStep(String name, Runnable runnable) {
        info(name);
        runnable.run();
    }

    default <T> T withinStep(String name, java.util.function.Supplier<T> supplier) {
        info(name);
        return supplier.get();
    }
}
