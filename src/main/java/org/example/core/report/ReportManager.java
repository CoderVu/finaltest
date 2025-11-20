package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.report.strategy.ExtentStrategyI;
import org.example.core.report.listener.IReportStrategyListener;
import org.example.enums.ReportType;

@Slf4j
public final class ReportManager {

    private ReportManager() {
    }

    private static volatile IReporter reporterInstance;
    private static volatile IReportStrategyListener strategyInstance;

    public static void initReport() {
        getReporter();
    }

    public static ReportType getActiveReportType() {
        String configuredType = Config.getPropertyOrDefault(Constants.REPORT_TYPE_PROPERTY, Constants.DEFAULT_REPORT);
        return ReportType.fromString(configuredType);
    }

    public static IReporter getReporter() {
        if (reporterInstance != null) {
            return reporterInstance;
        }
        synchronized (ReportManager.class) {
            if (reporterInstance != null) {
                return reporterInstance;
            }
            ReportType type = getActiveReportType();
            reporterInstance = ReporterFactory.createReporter(type);
            log.info("Created {} reporter instance", reporterInstance.getClass().getSimpleName());
            return reporterInstance;
        }
    }

    public static IReportStrategyListener selectStrategy() {
        if (strategyInstance != null) {
            return strategyInstance;
        }
        synchronized (ReportManager.class) {
            if (strategyInstance != null) {
                return strategyInstance;
            }
            ReportType type = getActiveReportType();
            log.info("Selected {} strategy (Output: {})", type.name(), type.getOutputPath());
            strategyInstance = new ExtentStrategyI();
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
