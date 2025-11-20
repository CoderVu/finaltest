package org.example.core.report.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.strategy.ExtentStrategyI;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ReportListener implements ITestListener, IConfigurationListener {
	private volatile List<IReportStrategyListener> delegates;

	public ReportListener() {
		log.info("ReportListener created (lazy init).");
	}

	private synchronized void ensureInitialized() {
		if (delegates != null) return;

		this.delegates = Collections.singletonList(new ExtentStrategyI());
		log.info("Using report strategy: EXTENT");
	}

	private void forEachDelegate(DelegateAction action) {
		ensureInitialized();
		for (IReportStrategyListener d : delegates) {
			try {
				action.apply(d);
			} catch (Throwable t) {
				log.warn("Report strategy {} threw exception: {}", d.getClass().getSimpleName(), t.getMessage(), t);
			}
		}
	}

	@FunctionalInterface
	private interface DelegateAction {
		void apply(IReportStrategyListener strategy) throws Throwable;
	}

	// ITestListener
	@Override 
	public void onTestStart(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestStart(result)); 
	}
	
	@Override 
	public void onTestSuccess(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestSuccess(result)); 
	}
	
	@Override 
	public void onTestFailure(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestFailure(result)); 
	}
	
	@Override 
	public void onTestSkipped(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestSkipped(result)); 
	}
	
	@Override 
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestFailedButWithinSuccessPercentage(result)); 
	}
	
	@Override 
	public void onTestFailedWithTimeout(ITestResult result) { 
		ensureInitialized();
		forEachDelegate(d -> d.onTestFailedWithTimeout(result)); 
	}
	
	@Override 
	public void onStart(ITestContext context) { 
		ensureInitialized();
		forEachDelegate(d -> d.onStart(context)); 
	}
	
	@Override 
	public void onFinish(ITestContext context) { 
		ensureInitialized();
		forEachDelegate(d -> d.onFinish(context)); 
	}

	// IConfigurationListener
	@Override 
	public void onConfigurationSuccess(ITestResult itr) { 
		ensureInitialized();
		forEachDelegate(d -> d.onConfigurationSuccess(itr)); 
	}
	
	@Override 
	public void onConfigurationFailure(ITestResult itr) { 
		ensureInitialized();
		forEachDelegate(d -> d.onConfigurationFailure(itr)); 
	}
	
	@Override 
	public void onConfigurationSkip(ITestResult itr) { 
		ensureInitialized();
		forEachDelegate(d -> d.onConfigurationSkip(itr)); 
	}
}
