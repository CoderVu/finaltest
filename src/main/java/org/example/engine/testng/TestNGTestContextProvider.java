package org.example.engine.testng;

import org.example.core.context.TestContext;
import org.example.core.context.TestContextProvider;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * TestNG implementation of TestContextProvider.
 */
public class TestNGTestContextProvider implements TestContextProvider {
    private static final ThreadLocal<ITestResult> FALLBACK_RESULT = new ThreadLocal<>();

    @Override
    public TestContext getCurrentContext() {
        ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            result = FALLBACK_RESULT.get();
        }
        if (result == null) {
            return null;
        }
        return new TestNGTestContext(result);
    }

    public static void setCurrentResult(ITestResult result) {
        FALLBACK_RESULT.set(result);
    }

    public static void clearCurrentResult() {
        FALLBACK_RESULT.remove();
    }
}
