package org.example.core.reporting;

import org.example.core.reporting.listeners.ReportingListener;

/**
 * IReportType interface for reporting implementations.
 * Each plugin implementation provides a unique identifier (type) and factory methods
 * to create Reporter and ReportingListener instances.
 * 
 * Users implement this interface to integrate their own reporting libraries.
 */
public interface IReportType {

    /**
     * Returns a unique identifier for this plugin (e.g., "extent", "allure", "custom").
     */
    String getType();

    /**
     * Creates a new Reporter instance for logging steps, screenshots, etc.
     */
    Reporter createReporter();

    /**
     * Creates a new ReportingListener instance for handling test lifecycle events.
     */
    ReportingListener createLifecycleListener();
}


