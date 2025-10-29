package org.example.core.report.strategy;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ReportType;

@Slf4j
public final class ReportStrategySelector {
    private ReportStrategySelector() {}

    public static ReportStrategy select() {
        ReportType type = ReportType.getConfigured();
        
        switch (type) {
            case EXTENT:
                log.info("Selected ExtentReports strategy (Output: {})", type.getOutputPath());
                return new ExtentReportStrategy();
            case JENKINS:
                log.info("Selected Jenkins strategy (Output: {})", type.getOutputPath());
                return new JenkinsReportStrategy();
            case ALLURE:
            default:
                log.info("Selected Allure strategy (Output: {})", type.getOutputPath());
                return new AllureReportStrategy();
        }
    }
}


