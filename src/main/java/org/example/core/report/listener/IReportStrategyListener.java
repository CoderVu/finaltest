package org.example.core.report.listener;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;

public interface IReportStrategyListener extends ITestListener, IConfigurationListener {
	void failStep(String name);
}
