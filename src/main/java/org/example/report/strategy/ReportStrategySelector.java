package org.example.report.strategy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReportStrategySelector {
    private ReportStrategySelector() {}

    public static ReportStrategy select() {
        String strategy = System.getProperty("report.strategy", System.getenv("REPORT_STRATEGY"));
        if (strategy == null || strategy.trim().isEmpty()) {
            strategy = "jenkins"; 
        }
        String normalized = strategy.trim().toLowerCase();
        switch (normalized) {
            case "extent":
                return new ExtentReportStrategy();
            case "jenkins":
                return new JenkinsReportStrategy();
            case "allure":
            default:
                return new AllureReportStrategy();
        }
    }
}


