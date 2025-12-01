package org.example.core.retry;

import lombok.Builder;
import lombok.Data;

/**
 * Context information for retry decision.
 * Framework agnostic - can be populated from TestNG, JUnit, or any framework.
 */
@Data
@Builder
public class RetryContext {
    /**
     * Test method name
     */
    private String testName;

    /**
     * Test class name
     */
    private String testClass;

    /**
     * Browser name (for parallel execution)
     */
    private String browser;

    /**
     * Additional context data (framework-specific)
     */
    private Object frameworkContext;
}



