package org.example.core.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Registry for test context provider.
 * Test engine adapters should register their provider here.
 */
@Slf4j
public final class TestContextRegistry {

    private static volatile TestContextProvider provider;

    private TestContextRegistry() {}

    public static void setProvider(TestContextProvider provider) {
        TestContextRegistry.provider = provider;
        log.info("Test context provider registered: {}", provider.getClass().getSimpleName());
    }

    public static TestContext getCurrentContext() {
        if (provider == null) {
            return null;
        }
        return provider.getCurrentContext();
    }

    public static void reset() {
        provider = null;
    }
}

