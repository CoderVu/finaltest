package org.example.testng.listeners;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportingManager;
import org.example.core.context.TestContextRegistry;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.testng.TestNGTestContextProvider;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Collections;
import java.util.List;

/**
 * TestNG-specific adapter that forwards TestNG events to the core reporting layer.
 */
@Slf4j
public class TestNGReporter implements ITestListener, IConfigurationListener {

    private volatile List<ReportingListener> delegates;

    static {
        // Register TestNG test context provider
        TestContextRegistry.setProvider(new TestNGTestContextProvider());
    }

    private void forEach(DelegateAction action) {
        if (delegates == null) {
            ReportingListener lifecycleListener = ReportingManager.getLifecycleListener();
            this.delegates = Collections.singletonList(lifecycleListener);
            log.info("TestNGReporter wired {}", lifecycleListener.getClass().getSimpleName());
        }
        for (ReportingListener delegate : delegates) {
            action.apply(delegate);
        }
    }

    @FunctionalInterface
    private interface DelegateAction {
        void apply(ReportingListener listener);
    }

    private String extractTestName(ITestResult result) {
        if (result == null) return "<unknown>";
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
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

