package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ReportType;
import org.example.core.report.impl.AllureReporter;
import org.example.core.report.impl.ExtentReporter;
import org.example.core.report.impl.JenkinsReporter;

@Slf4j
public final class ReporterManager {

    private static volatile TestReporter activeReporter;
    private static volatile ReportType activeType;

    private ReporterManager() { }

    public static TestReporter get() {
        if (activeReporter == null) {
            synchronized (ReporterManager.class) {
                if (activeReporter == null) {
                    activeReporter = create();
                }
            }
        }
        return activeReporter;
    }

    public static ReportType getActiveType() {
        if (activeType == null) {
            get();
        }
        return activeType;
    }

    public static void reset() {
        synchronized (ReporterManager.class) {
            activeReporter = null;
            activeType = null;
        }
    }

    private static TestReporter create() {
        activeType = ReportType.getConfigured();
        switch (activeType) {
            case EXTENT:
                log.info("Using ExtentReporter for test steps (Output: {})", activeType.getOutputPath());
                return new ExtentReporter();
            case JENKINS:
                log.info("Using JenkinsReporter for test steps (Output: {})", activeType.getOutputPath());
                return new JenkinsReporter();
            case ALLURE:
            default:
                log.info("Using AllureReporter for test steps (Output: {})", activeType.getOutputPath());
                return new AllureReporter();
        }
    }

    /**
     * Get Allure reporter instance directly
     * Usage: protected TestReporter reporter = ReporterManager.getAllureReport();
     */
    public static TestReporter getAllureReport() {
        log.info("Using AllureReporter for test steps (Output: {})", ReportType.ALLURE.getOutputPath());
        return new AllureReporter();
    }

    /**
     * Get Extent reporter instance directly
     * Usage: protected TestReporter reporter = ReporterManager.getExtentReport();
     */
    public static TestReporter getExtentReport() {
        log.info("Using ExtentReporter for test steps (Output: {})", ReportType.EXTENT.getOutputPath());
        return new ExtentReporter();
    }

    /**
     * Get Jenkins reporter instance directly
     * Usage: protected TestReporter reporter = ReporterManager.getJenkinsReport();
     */
    public static TestReporter getJenkinsReport() {
        log.info("Using JenkinsReporter for test steps (Output: {})", ReportType.JENKINS.getOutputPath());
        return new JenkinsReporter();
    }
}


