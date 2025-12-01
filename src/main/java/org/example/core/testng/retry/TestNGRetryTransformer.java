package org.example.core.testng.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.core.retry.NoRetry;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG adapter for injecting RetryService via TestNGRetryAnalyzer.
 * Bridges TestNG IAnnotationTransformer interface to framework-agnostic retry mechanism.
 */
@Slf4j
public class TestNGRetryTransformer implements IAnnotationTransformer {

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {

        String testClassName = testClass != null ? testClass.getSimpleName() : "<unknown>";
        String testMethodName = testMethod != null ? testMethod.getName() : "<unknown>";
        String fullTestName = testClassName + "." + testMethodName;

        // Skip if test has @NoRetry annotation
        if (testMethod != null && testMethod.isAnnotationPresent(NoRetry.class)) {
            log.info("[TRANSFORM] Test {} has @NoRetry annotation - skipping retry injection", fullTestName);
            return;
        }

        Class<? extends IRetryAnalyzer> configuredRetry = annotation.getRetryAnalyzerClass();
        String existingRetryName = configuredRetry != null ? configuredRetry.getSimpleName() : "null";

        // Force inject TestNGRetryAnalyzer if:
        // 1. No retry analyzer configured (null)
        // 2. Default IRetryAnalyzer.class
        // 3. DisabledRetryAnalyzer (TestNG default when no retry is configured)
        boolean shouldInject = configuredRetry == null
                || configuredRetry == IRetryAnalyzer.class
                || "DisabledRetryAnalyzer".equals(existingRetryName);

        if (shouldInject) {
            annotation.setRetryAnalyzer(TestNGRetryAnalyzer.class);
            log.info("[TRANSFORM] Injected TestNGRetryAnalyzer into test: {} (replaced: {})",
                    fullTestName, existingRetryName);
        } else {
            log.info("[TRANSFORM] Test {} already has custom retry analyzer: {} (keeping it)",
                    fullTestName, existingRetryName);
        }
    }
}

