package org.example.report.strategy;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;

/**
 * Abstraction for pluggable report backends.
 * Implementations should be stateless per test run or manage their own lifecycle.
 */
public interface ReportStrategy extends ITestListener, IConfigurationListener {
}


