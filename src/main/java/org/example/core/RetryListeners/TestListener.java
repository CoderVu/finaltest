package org.example.core.RetryListeners;

import lombok.extern.slf4j.Slf4j;
import org.example.core.annotations.NoRetry;
import org.example.core.reporting.listeners.ReportingListener;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Slf4j
public class TestListener extends ReportingListener implements IAnnotationTransformer {

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {

        String testClassName = testClass != null ? testClass.getSimpleName() : "<unknown>";
        String testMethodName = testMethod != null ? testMethod.getName() : "<unknown>";
        String fullTestName = testClassName + "." + testMethodName;

        if (testMethod != null && testMethod.isAnnotationPresent(NoRetry.class)) {
            return;
        }

        Class<? extends IRetryAnalyzer> configuredRetry = annotation.getRetryAnalyzerClass();
        String existingRetryName = configuredRetry != null ? configuredRetry.getSimpleName() : "null";
        
        // Force inject RetryAnalyzer if:
        // 1. No retry analyzer configured (null)
        // 2. Default IRetryAnalyzer.class
        // 3. DisabledRetryAnalyzer (TestNG default when no retry is configured)
        boolean shouldInject = configuredRetry == null 
                || configuredRetry == IRetryAnalyzer.class
                || "DisabledRetryAnalyzer".equals(existingRetryName);
        
        if (shouldInject) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
            log.info("[TRANSFORM] Injected RetryAnalyzer into test: {} (replaced: {})",
                    fullTestName, existingRetryName);
        } else {
            log.info("[TRANSFORM] Test {} already has custom retry analyzer: {} (keeping it)",
                    fullTestName, existingRetryName);
        }
    }
}
