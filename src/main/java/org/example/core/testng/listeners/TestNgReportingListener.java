package org.example.core.testng.listeners;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.reporting.listeners.CoreReportingListener;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Collections;
import java.util.List;

/**
 * TestNG-specific adapter that forwards TestNG events to the core reporting layer.
 *
 * This class should contain all TestNG dependencies, while the core reporting
 * contracts remain engine-agnostic.
 */
@Slf4j
public class TestNgReportingListener implements ITestListener, IConfigurationListener {

    private volatile List<CoreReportingListener> delegates;

    private void ensureInitialized() {
        if (delegates == null) {
            // ReportingManager.getLifecycleListener() returns a CoreReportingListener.
            CoreReportingListener lifecycleListener = (CoreReportingListener) ReportingManager.getLifecycleListener();
            this.delegates = Collections.singletonList(lifecycleListener);
            log.info("TestNgReportingListener wired {}", lifecycleListener.getClass().getSimpleName());
        }
    }

    private void forEach(DelegateAction action) {
        ensureInitialized();
        for (CoreReportingListener delegate : delegates) {
            try {
                action.apply(delegate);
            } catch (Throwable t) {
                log.warn("Lifecycle listener {} threw exception: {}", delegate.getClass().getSimpleName(), t.getMessage(), t);
            }
        }
    }

    @FunctionalInterface
    private interface DelegateAction {
        void apply(CoreReportingListener listener) throws Throwable;
    }

    private String extractTestName(ITestResult result) {
        if (result == null) return "<unknown>";
        try {
            return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        } catch (Exception e) {
            return result.getMethod() != null ? result.getMethod().getMethodName() : result.getName();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = extractTestName(result);
        forEach(d -> d.onTestStart(name));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String name = extractTestName(result);
        forEach(d -> d.onTestSuccess(name));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = extractTestName(result);
        Throwable t = result != null ? result.getThrowable() : null;
        forEach(d -> d.onTestFailure(name, t));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String name = extractTestName(result);
        forEach(d -> d.onTestSkipped(name));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String name = extractTestName(result);
        forEach(d -> d.onTestFailure(name, result != null ? result.getThrowable() : null));
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        String name = extractTestName(result);
        forEach(d -> d.onTestFailure(name, result != null ? result.getThrowable() : null));
    }

    @Override
    public void onStart(ITestContext context) {
        String suiteName = context != null ? context.getName() : "<suite>";
        forEach(d -> d.onStart(suiteName));
    }

    @Override
    public void onFinish(ITestContext context) {
        String suiteName = context != null ? context.getName() : "<suite>";
        forEach(d -> d.onFinish(suiteName));
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        String name = extractTestName(itr);
        forEach(d -> d.onConfigurationSuccess(name));
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        String name = extractTestName(itr);
        forEach(d -> d.onConfigurationFailure(name));
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        String name = extractTestName(itr);
        forEach(d -> d.onConfigurationSkip(name));
    }
}


