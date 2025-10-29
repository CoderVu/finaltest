package org.example.report;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ReportType;
import org.example.report.impl.AllureReporter;
import org.example.report.impl.ExtentReporter;
import org.example.report.impl.JenkinsReporter;

/**
 * Factory to create the appropriate TestReporter based on configuration.
 * Uses ReportType enum to avoid hardcoded strings.
 */
@Slf4j
public class ReporterFactory {

    private static TestReporter instance;
    private static ReportType currentType;

    public static TestReporter getInstance() {
        if (instance == null) {
            instance = create();
        }
        return instance;
    }

    /**
     * Get the currently configured report type.
     */
    public static ReportType getCurrentType() {
        if (currentType == null) {
            currentType = ReportType.getConfigured();
        }
        return currentType;
    }

    private static TestReporter create() {
        currentType = ReportType.getConfigured();
        
        switch (currentType) {
            case EXTENT:
                log.info("Using ExtentReporter for test steps (Output: {})", currentType.getOutputPath());
                return new ExtentReporter();
            case JENKINS:
                log.info("Using JenkinsReporter for test steps (Output: {})", currentType.getOutputPath());
                return new JenkinsReporter();
            case ALLURE:
            default:
                log.info("Using AllureReporter for test steps (Output: {})", currentType.getOutputPath());
                return new AllureReporter();
        }
    }
}
