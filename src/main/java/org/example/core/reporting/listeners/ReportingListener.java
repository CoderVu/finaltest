package org.example.core.reporting.listeners;

public interface ReportingListener {

    void onTestStart(String testName);

    void onTestSuccess(String testName);

    void onTestFailure(String testName, Throwable error);

    void onTestSkipped(String testName);

    void onStart(String suiteName);

    void onFinish(String suiteName);

    void onConfigurationSuccess(String configName);

    void onConfigurationFailure(String configName);

    void onConfigurationSkip(String configName);
}
