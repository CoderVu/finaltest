package org.example.engine.junit.listener;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.context.TestContextRegistry;
import org.example.core.reporting.ReportManager;
import org.example.core.reporting.listeners.ReportingListener;
import org.example.engine.junit.JUnitTestContext;
import org.example.engine.junit.JUnitTestContextProvider;
import org.example.enums.TestEngine;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JUnitReporter implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, TestWatcher {

    private volatile List<ReportingListener> delegates;
    private static final boolean ENABLED = Config.getTestEngine() == TestEngine.JUNIT;

    static {
        if (ENABLED) {
            TestContextRegistry.setProvider(new JUnitTestContextProvider());
        }
    }

    private void forEach(DelegateAction action) {
        if (!ENABLED) {
            return;
        }
        if (delegates == null) {
            ReportingListener lifecycleListener = ReportManager.getLifecycleListener();
            this.delegates = Collections.singletonList(lifecycleListener);
            log.info("JUnitReporter wired {}", lifecycleListener.getClass().getSimpleName());
        }
        for (ReportingListener delegate : delegates) {
            action.apply(delegate);
        }
    }

    @FunctionalInterface
    private interface DelegateAction {
        void apply(ReportingListener listener);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        String suiteName = context.getRequiredTestClass().getSimpleName();
        forEach(d -> d.onStart(suiteName));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        JUnitTestContext testContext = new JUnitTestContext();
        String className = context.getRequiredTestClass().getName();
        String methodName = resolveMethodName(context);
        String browser = System.getProperty("browser", "chrome");

        testContext.setAttribute("test.class", className);
        testContext.setAttribute("test.method", methodName);
        testContext.setAttribute("test.browser", browser);
        JUnitTestContextProvider.setCurrent(testContext);

        forEach(d -> d.onTestStart(className + "." + methodName));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String name = context.getRequiredTestClass().getName() + "." + resolveMethodName(context);
        forEach(d -> d.onTestSuccess(name));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        JUnitTestContext current = (JUnitTestContext) TestContextRegistry.getCurrentContext();
        if (current != null) {
            current.setAttribute("test.throwable", cause);
        }
        String name = context.getRequiredTestClass().getName() + "." + resolveMethodName(context);
        forEach(d -> d.onTestFailure(name, cause));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String name = context.getRequiredTestClass().getName() + "." + resolveMethodName(context);
        forEach(d -> d.onTestSkipped(name));
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String name = context.getRequiredTestClass().getName() + "." + resolveMethodName(context);
        forEach(d -> d.onTestSkipped(name));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        JUnitTestContextProvider.clear();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        String suiteName = context.getRequiredTestClass().getSimpleName();
        forEach(d -> d.onFinish(suiteName));
    }

    private String resolveMethodName(ExtensionContext context) {
        return context.getTestMethod().map(m -> m.getName()).orElse("<unknown>");
    }
}
