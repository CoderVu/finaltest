package org.example.core.reporting.listeners;

public interface ReportingListener {

    default void onTestStart(String testName) {}

    default void onTestSuccess(String testName) {}

    default void onTestFailure(String testName, Throwable error) {}

    default void onTestSkipped(String testName) {}

    default void onStart(String suiteName) {}

    default void onFinish(String suiteName) {}

    default void onConfigurationSuccess(String configName) {}

    default void onConfigurationFailure(String configName) {}

    default void onConfigurationSkip(String configName) {}
}
