package org.example.core.context;

/**
 * Provider for test context.
 * Implementations should be provided by test engine adapters (TestNG, JUnit, etc.).
 */
public interface TestContextProvider {

    /**
     * Gets the current test context, or null if not in a test execution context.
     */
    TestContext getCurrentContext();
}

