package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.report.strategy.AllureStrategyI;
import org.example.core.report.strategy.ExtentStrategyI;
import org.example.core.report.listener.IReportStrategyListener;
import org.example.enums.ReportType;

import java.util.List;

@Slf4j
public final class ReportManager {

    private ReportManager() {
    }

    private static volatile IReporter reporterInstance;
    private static volatile IReportStrategyListener strategyInstance;

    /**
     * Initialize report system. Must be called before using reporters.
     */
    public static void initReport() {
        // Initialize reporter instance
        getReporter();
    }

    public static IReporter getReporter() {
        List<String> types = Constants.getReportTypes();
        return getReporter(types.isEmpty() ? "" : types.get(0));
    }

    public static ReportType parseReportType(String reportTypes) {
        String input = reportTypes == null ? "" : reportTypes.trim();
        if (input.isEmpty()) {
            List<String> types = Constants.getReportTypes();
            input = types.isEmpty() ? "" : types.get(0);
        }
        if (input.isEmpty()) {
            return ReportType.ALLURE;
        }
        String[] parts = input.split("[,;]");
        if (parts.length > 0) {
            ReportType type = ReportType.fromString(parts[0].trim());
            if (type != null) {
                return type;
            }
        }
        return ReportType.ALLURE;
    }

    public static IReporter getReporter(String reportTypes) {
        if (reporterInstance != null) {
            return reporterInstance;
        }
        synchronized (ReportManager.class) {
            if (reporterInstance != null) {
                return reporterInstance;
            }
            String input = reportTypes == null ? "" : reportTypes.trim();
            if (input.isEmpty()) {
                List<String> types = Constants.getReportTypes();
                input = types.isEmpty() ? "" : types.get(0);
            }
            ReportType type = ReportType.ALLURE;
            if (!input.isEmpty()) {
                String[] parts = input.split("[,;]");
                if (parts.length > 0) {
                    ReportType parsed = ReportType.fromString(parts[0].trim());
                    if (parsed != null) {
                        type = parsed;
                    }
                }
            }
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
            ReportType type = parseReportType(null);
            switch (type) {
                case EXTENT:
                    log.info("Selected Extent strategy (Output: {})", type.getOutputPath());
                    strategyInstance = new ExtentStrategyI();
                    break;
                case ALLURE:
                default:
                    log.info("Selected Allure strategy (Output: {})", type.getOutputPath());
                    strategyInstance = new AllureStrategyI();
                    break;
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
