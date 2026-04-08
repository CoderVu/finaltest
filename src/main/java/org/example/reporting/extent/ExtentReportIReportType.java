package org.example.reporting.extent;

import org.example.core.reporting.IReportType;
import org.example.core.reporting.Reporter;
import org.example.core.reporting.listeners.ReportingListener;

public class ExtentReportIReportType implements IReportType {

    @Override
    public String getType() {
        return "extent";
    }

    @Override
    public Reporter createReporter() {
        return new ExtentReporter();
    }

    @Override
    public ReportingListener createLifecycleListener() {
        return new ExtentReportLifecycle();
    }
}

