package org.example.core.reporting.plugin;

import org.example.core.reporting.ReportClient;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.example.enums.ReportType;

public interface ReportPlugin {

    ReportType getType();

    ReportClient createReporter();

    ReportingLifecycleListener createLifecycleListener();
}


