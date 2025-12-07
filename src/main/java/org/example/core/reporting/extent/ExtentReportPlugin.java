package org.example.core.reporting.extent;

import org.example.core.reporting.Plugin;
import org.example.core.reporting.ReportClient;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.enums.ReportType;

public class ExtentReportPlugin implements Plugin {

    @Override
    public ReportType getType() {
        return ReportType.EXTENT;
    }

    @Override
    public ReportClient createReporter() {
        return new ExtentReportClient();
    }

    @Override
    public ReportingListener createLifecycleListener() {
        return new ExtentReportLifecycle();
    }
}


