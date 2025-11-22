package org.example.core.reporting.extent;

import org.example.core.reporting.ReportClient;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.example.core.reporting.plugin.ReportPlugin;
import org.example.enums.ReportType;

public class ExtentReportPlugin implements ReportPlugin {

    @Override
    public ReportType getType() {
        return ReportType.EXTENT;
    }

    @Override
    public ReportClient createReporter() {
        return new ExtentReportClient();
    }

    @Override
    public ReportingLifecycleListener createLifecycleListener() {
        return new ExtentReportLifecycle();
    }
}


