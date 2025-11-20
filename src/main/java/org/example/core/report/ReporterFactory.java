package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.impl.ExtentReporter;
import org.example.enums.ReportType;

@Slf4j
public class ReporterFactory {

    private static final ThreadLocal<AbstractReporter> THREAD_LOCAL = new ThreadLocal<>();

    public static IReporter createReporter(ReportType type) {
        log.debug("Creating Reporter for type {}", type);
        AbstractReporter reporter = THREAD_LOCAL.get();
        if (reporter == null || reporter.getReportType() != type) {
            reporter = new ExtentReporter();
            THREAD_LOCAL.set(reporter);
        }
        log.debug("Reporter initialized for type: {}", type);
        return reporter;
    }
    public static AbstractReporter getCurrentReporter() {
        return THREAD_LOCAL.get();
    }

    public static void clearReporter() {
        THREAD_LOCAL.remove();
    }
}

