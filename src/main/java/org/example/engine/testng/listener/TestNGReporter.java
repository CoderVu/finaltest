package org.example.engine.testng.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.context.TestContextRegistry;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.engine.testng.TestNGTestContextProvider;
import org.example.enums.TestEngine;
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
    private static final boolean ENABLED = Config.getTestEngine() == TestEngine.TESTNG;

    static {
        if (ENABLED) {
            TestContextRegistry.setProvider(new TestNGTestContextProvider());
        }
    }

    private void forEach(DelegateAction action) {
        if (!ENABLED) {
            return;
        }
        if (delegates == null) {
            ReportingListener lifecycleListener = ReportManager.getLifecycleListener();
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

    private void dispatch(ITestResult result, DelegateAction action) {
        try {
            TestNGTestContextProvider.setCurrentResult(result);
            forEach(action);
        } finally {
            TestNGTestContextProvider.clearCurrentResult();
        }
    }

    private String extractTestName(ITestResult result) {
        if (result == null) {
            return "<unknown>";
        }
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = extractTestName(result);
        dispatch(result, d -> d.onTestStart(name));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String name = extractTestName(result);
        dispatch(result, d -> d.onTestSuccess(name));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = extractTestName(result);
        Throwable t = result != null ? result.getThrowable() : null;
        dispatch(result, d -> d.onTestFailure(name, t));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String name = extractTestName(result);
        dispatch(result, d -> d.onTestSkipped(name));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String name = extractTestName(result);
        dispatch(result, d -> d.onTestFailure(name, result != null ? result.getThrowable() : null));
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        String name = extractTestName(result);
        dispatch(result, d -> d.onTestFailure(name, result != null ? result.getThrowable() : null));
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
        dispatch(itr, d -> d.onConfigurationSuccess(name));
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        String name = extractTestName(itr);
        dispatch(itr, d -> d.onConfigurationFailure(name));
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        String name = extractTestName(itr);
        dispatch(itr, d -> d.onConfigurationSkip(name));
    }
}
