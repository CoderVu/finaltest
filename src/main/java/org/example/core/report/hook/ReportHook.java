package org.example.core.report.hook;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ReportManager;
import org.example.core.report.strategy.ReportStrategy;
import org.example.core.report.strategy.AllureStrategy;
import org.example.core.report.strategy.ExtentStrategy;
import org.example.core.report.strategy.JenkinsStrategy;
import org.example.enums.ReportType;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ReportHook implements ITestListener, IConfigurationListener {
	// delegates now lazily initialized
	private volatile List<ReportStrategy> delegates;

	public ReportHook() {
		// Do not parse config here — Config may not be initialized yet when TestNG constructs listeners.
		log.info("ReportHook created (lazy init).");
	}

	// ensure delegates are created once when first needed
	private synchronized void ensureInitialized() {
		if (delegates != null) return;

		List<ReportType> types = ReportManager.parseReportTypes(null);
		List<ReportStrategy> list = new ArrayList<>();
		for (ReportType t : types) {
			switch (t) {
				case EXTENT:
					list.add(new ExtentStrategy());
					break;
				case JENKINS:
					list.add(new JenkinsStrategy());
					break;
				case ALLURE:
				default:
					list.add(new AllureStrategy());
					break;
			}
		}
		this.delegates = Collections.unmodifiableList(list);
		String names = types.stream().map(Enum::name).collect(Collectors.joining(", "));
		log.info("Using report strategies: {}", names);
	}

	private void forEachDelegate(DelegateAction action) {
		ensureInitialized(); // ensure ready before delegating
		for (ReportStrategy d : delegates) {
			try {
				action.apply(d);
			} catch (Throwable t) {
				log.warn("Report strategy {} threw exception: {}", d.getClass().getSimpleName(), t.getMessage(), t);
			}
		}
	}

	@FunctionalInterface
	private interface DelegateAction {
		void apply(ReportStrategy strategy) throws Throwable;
	}

	// ITestListener
	@Override public void onTestStart(ITestResult result) { forEachDelegate(d -> d.onTestStart(result)); }
	@Override public void onTestSuccess(ITestResult result) { forEachDelegate(d -> d.onTestSuccess(result)); }
	@Override public void onTestFailure(ITestResult result) { forEachDelegate(d -> d.onTestFailure(result)); }
	@Override public void onTestSkipped(ITestResult result) { forEachDelegate(d -> d.onTestSkipped(result)); }
	@Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { forEachDelegate(d -> d.onTestFailedButWithinSuccessPercentage(result)); }
	@Override public void onTestFailedWithTimeout(ITestResult result) { forEachDelegate(d -> d.onTestFailedWithTimeout(result)); }
	@Override public void onStart(ITestContext context) { forEachDelegate(d -> d.onStart(context)); }
	@Override public void onFinish(ITestContext context) { forEachDelegate(d -> d.onFinish(context)); }

	// IConfigurationListener
	@Override public void onConfigurationSuccess(ITestResult itr) { forEachDelegate(d -> d.onConfigurationSuccess(itr)); }
	@Override public void onConfigurationFailure(ITestResult itr) { forEachDelegate(d -> d.onConfigurationFailure(itr)); }
	@Override public void onConfigurationSkip(ITestResult itr) { forEachDelegate(d -> d.onConfigurationSkip(itr)); }
}
