package org.example.core.reporting.listeners;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.lifecycle.ReportingLifecycleListener;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ReportingListener implements ITestListener, IConfigurationListener {

    private volatile List<ReportingLifecycleListener> delegates;

    private void ensureInitialized() {
        if (delegates == null) {
            ReportingLifecycleListener lifecycleListener = ReportingManager.getLifecycleListener();
            this.delegates = Collections.singletonList(lifecycleListener);
            log.info("ReportingListener wired {}", lifecycleListener.getClass().getSimpleName());
        }
    }

    private void forEach(DelegateAction action) {
        ensureInitialized();
        for (ReportingLifecycleListener delegate : delegates) {
            try {
                action.apply(delegate);
            } catch (Throwable t) {
                log.warn("Lifecycle listener {} threw exception: {}", delegate.getClass().getSimpleName(), t.getMessage(), t);
            }
        }
    }

    @FunctionalInterface
    private interface DelegateAction {
        void apply(ReportingLifecycleListener listener) throws Throwable;
    }

    @Override
    public void onTestStart(ITestResult result) {
        forEach(d -> d.onTestStart(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        forEach(d -> d.onTestSuccess(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        forEach(d -> d.onTestFailure(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        forEach(d -> d.onTestSkipped(result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        forEach(d -> d.onTestFailedButWithinSuccessPercentage(result));
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        forEach(d -> d.onTestFailedWithTimeout(result));
    }

    @Override
    public void onStart(ITestContext context) {
        forEach(d -> d.onStart(context));
    }

    @Override
    public void onFinish(ITestContext context) {
        forEach(d -> d.onFinish(context));
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        forEach(d -> d.onConfigurationSuccess(itr));
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        forEach(d -> d.onConfigurationFailure(itr));
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        forEach(d -> d.onConfigurationSkip(itr));
    }
}


