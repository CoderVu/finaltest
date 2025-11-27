package org.example.core.reporting;

import org.example.enums.ReportType;

import java.util.function.Supplier;

/**
 * Primary interface for reporting adapters. Each implementation delegates
 * to a concrete reporting library (Extent, Allure, …) but exposes a stable API
 * to the rest of the framework.
 */
public interface ReportClient {

    void logStep(String message);

    void info(String message);

    void logFail(String message, Throwable error);

    void attachScreenshot(String name);

    void childStep(String name, Runnable runnable);

    <T> T childStep(String name, Supplier<T> supplier);

    ReportType getReportType();

    default boolean isInStep() {
        return false;
    }
}


