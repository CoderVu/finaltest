package org.example.testng;

import org.example.core.context.TestContext;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;

/**
 * TestNG implementation of TestContext.
 */
public class TestNGTestContext implements TestContext {

    private final ITestResult testResult;
    private final Map<String, Object> additionalAttributes = new HashMap<>();

    public TestNGTestContext(ITestResult testResult) {
        this.testResult = testResult;
        // Extract TestNG-specific info and store as attributes
        if (testResult != null) {
            additionalAttributes.put("test.class", testResult.getTestClass() != null ? testResult.getTestClass().getName() : null);
            additionalAttributes.put("test.method", testResult.getMethod() != null ? testResult.getMethod().getMethodName() : null);
            additionalAttributes.put("test.throwable", testResult.getThrowable());
            
            // Extract browser parameter
            try {
                String browser = testResult.getTestContext()
                        .getCurrentXmlTest()
                        .getParameter("browser");
                if (browser != null && !browser.isBlank()) {
                    additionalAttributes.put("test.browser", browser);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public Object getAttribute(String key) {
        // Check additional attributes first
        if (additionalAttributes.containsKey(key)) {
            return additionalAttributes.get(key);
        }
        // Then check TestNG result attributes
        return testResult != null ? testResult.getAttribute(key) : null;
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (testResult != null) {
            testResult.setAttribute(key, value);
        }
        // Also store in additional attributes for consistency
        additionalAttributes.put(key, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>(additionalAttributes);
        if (testResult != null) {
            for (String key : testResult.getAttributeNames()) {
                attributes.put(key, testResult.getAttribute(key));
            }
        }
        return attributes;
    }

    public ITestResult getTestResult() {
        return testResult;
    }
}

