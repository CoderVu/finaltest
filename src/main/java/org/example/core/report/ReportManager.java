package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.impl.AllureTestReporter;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.core.report.impl.JenkinsTestReporter;
import org.example.core.report.strategy.AllureStrategy;
import org.example.core.report.strategy.ExtentStrategy;
import org.example.core.report.strategy.JenkinsStrategy;
import org.example.core.report.strategy.ReportStrategy;
import org.example.enums.ReportType;

@Slf4j
public final class ReportManager {
    private ReportManager() {}

    private static volatile ITestReporter reporterInstance;
    private static volatile ReportStrategy strategyInstance;

    public static ITestReporter getReporter() {
        if (reporterInstance == null) {
            synchronized (ReportManager.class) {
                if (reporterInstance == null) {
                    ReportType type = ReportType.getConfigured();
                    switch (type) {
                        case EXTENT:
                            reporterInstance = new ExtentTestReporter();
                            log.debug("Created ExtentTestReporter instance");
                            break;
                        case JENKINS:
                            reporterInstance = new JenkinsTestReporter();
                            log.debug("Created JenkinsTestReporter instance");
                            break;
                        case ALLURE:
                        default:
                            reporterInstance = new AllureTestReporter();
                            log.debug("Created AllureTestReporter instance");
                            break;
                    }
                }
            }
        }
        return reporterInstance;
    }

    public static ReportStrategy selectStrategy() {
        if (strategyInstance == null) {
            synchronized (ReportManager.class) {
                if (strategyInstance == null) {
                    ReportType type = ReportType.getConfigured();
                    switch (type) {
                        case EXTENT:
                            log.info("Selected Extent strategy (Output: {})", type.getOutputPath());
                            strategyInstance = new ExtentStrategy();
                            break;
                        case JENKINS:
                            log.info("Selected Jenkins strategy (Output: {})", type.getOutputPath());
                            strategyInstance = new JenkinsStrategy();
                            break;
                        case ALLURE:
                        default:
                            log.info("Selected Allure strategy (Output: {})", type.getOutputPath());
                            strategyInstance = new AllureStrategy();
                            break;
                    }
                }
            }
        }
        return strategyInstance;
    }

    public static void reset() {
        synchronized (ReportManager.class) {
            reporterInstance = null;
            strategyInstance = null;
        }
    }
}


