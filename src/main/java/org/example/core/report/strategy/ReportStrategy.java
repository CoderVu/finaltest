package org.example.core.report.strategy;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;

public interface ReportStrategy extends ITestListener, IConfigurationListener {
	void failStep(String name);
}
