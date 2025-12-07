package org.example.core.context;

import java.util.Map;

/**
 * Framework-agnostic test context abstraction.
 * Allows core code to access test information without depending on TestNG/JUnit.
 */
public interface TestContext {

    /**
     * Gets an attribute from the test context.
     */
    Object getAttribute(String key);

    /**
     * Sets an attribute in the test context.
     */
    void setAttribute(String key, Object value);

    /**
     * Gets all attributes as a map.
     */
    Map<String, Object> getAttributes();
}

