package org.example.core.reporting.lifecycle;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;

/**
 * Bridges TestNG callbacks into a specific reporting backend.
 */
public interface ReportingLifecycleListener extends ITestListener, IConfigurationListener {

    void failStep(String stepName);
}


