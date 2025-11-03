package org.example.core.report.hook;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ReportManager;
import org.example.core.report.strategy.ReportStrategy;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class ReportHook implements ITestListener, IConfigurationListener {
    private final ReportStrategy delegate;

    public ReportHook() {
        this.delegate = ReportManager.selectStrategy();
        log.info("Using report strategy: {}", delegate.getClass().getSimpleName());
    }

    // ITestListener
    @Override public void onTestStart(ITestResult result) { delegate.onTestStart(result); }
    @Override public void onTestSuccess(ITestResult result) { delegate.onTestSuccess(result); }
    @Override public void onTestFailure(ITestResult result) { delegate.onTestFailure(result); }
    @Override public void onTestSkipped(ITestResult result) { delegate.onTestSkipped(result); }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { delegate.onTestFailedButWithinSuccessPercentage(result); }
    @Override public void onTestFailedWithTimeout(ITestResult result) { delegate.onTestFailedWithTimeout(result); }
    @Override public void onStart(ITestContext context) { delegate.onStart(context); }
    @Override public void onFinish(ITestContext context) { delegate.onFinish(context); }

    // IConfigurationListener
    @Override public void onConfigurationSuccess(ITestResult itr) { delegate.onConfigurationSuccess(itr); }
    @Override public void onConfigurationFailure(ITestResult itr) { delegate.onConfigurationFailure(itr); }
    @Override public void onConfigurationSkip(ITestResult itr) { delegate.onConfigurationSkip(itr); }
}
