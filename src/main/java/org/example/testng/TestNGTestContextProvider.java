package org.example.testng;

import org.example.core.context.TestContext;
import org.example.core.context.TestContextProvider;
import org.testng.Reporter;

/**
 * TestNG implementation of TestContextProvider.
 */
public class TestNGTestContextProvider implements TestContextProvider {

    @Override
    public TestContext getCurrentContext() {
        org.testng.ITestResult result = Reporter.getCurrentTestResult();
        if (result == null) {
            return null;
        }
        return new TestNGTestContext(result);
    }
}

