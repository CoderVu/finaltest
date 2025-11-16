package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.impl.AllureReporter;
import org.example.core.report.impl.ExtentReporter;
import org.example.enums.ReportType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class ReporterFactory {

    private static final Map<ReportType, Supplier<AbstractReporter>> REPORTER_MAP = new EnumMap<>(ReportType.class);
    private static final ThreadLocal<AbstractReporter> THREAD_LOCAL = new ThreadLocal<>();

    static {
        REPORTER_MAP.put(ReportType.ALLURE, AllureReporter::new);
        REPORTER_MAP.put(ReportType.EXTENT, ExtentReporter::new);
    }

    public static IReporter createReporter(ReportType type) {
        log.debug("Creating Reporter for type {}", type);
        AbstractReporter reporter = THREAD_LOCAL.get();
        if (reporter == null || reporter.getReportType() != type) {
            reporter = REPORTER_MAP.getOrDefault(type, AllureReporter::new).get();
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

