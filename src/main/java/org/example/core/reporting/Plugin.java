package org.example.core.reporting;

import org.example.core.reporting.listeners.ReportingListener;
import org.example.enums.ReportType;

public interface Plugin {

    ReportType getType();

    ReportClient createReporter();

    ReportingListener createLifecycleListener();
}


