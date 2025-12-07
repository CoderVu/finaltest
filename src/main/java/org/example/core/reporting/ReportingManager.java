package org.example.core.reporting;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.enums.ReportType;

@Slf4j
public final class ReportingManager {

    private static volatile ReportClient reportClient;
    private static volatile ReportingListener lifecycleListener;
    private static volatile Plugin activePlugin;

    private ReportingManager() {}

    public static void initReporting() {
        getReportClient();
    }

    public static ReportType getActiveReportType() {
        String configuredType = Config.getPropertyOrDefault(Constants.REPORT_TYPE_PROPERTY, Constants.DEFAULT_REPORT);
        return ReportType.fromString(configuredType);
    }

    public static ReportClient getReportClient() {
        if (reportClient == null) {
            ReportType type = getActiveReportType();
            Plugin plugin = ReportPluginRegistry.getPlugin(type);
            reportClient = plugin.createReporter();
            log.info("Initialized {} report client", reportClient.getClass().getSimpleName());
        }
        return reportClient;
    }

    public static ReportingListener getLifecycleListener() {
        if (lifecycleListener == null) {
            Plugin plugin = getActivePlugin();
            lifecycleListener = plugin.createLifecycleListener();
            log.info("Using {} lifecycle listener", lifecycleListener.getClass().getSimpleName());
        }
        return lifecycleListener;
    }

    private static Plugin getActivePlugin() {
        if (activePlugin == null) {
            ReportType type = getActiveReportType();
            activePlugin = ReportPluginRegistry.getPlugin(type);
        }
        return activePlugin;
    }

    public static void reset() {
        reportClient = null;
        lifecycleListener = null;
        activePlugin = null;
    }
}


