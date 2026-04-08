package org.example.engine.junit;

import org.example.core.context.TestContext;
import org.example.core.context.TestContextProvider;

public class JUnitTestContextProvider implements TestContextProvider {

    private static final ThreadLocal<JUnitTestContext> CURRENT = new ThreadLocal<>();

    @Override
    public TestContext getCurrentContext() {
        return CURRENT.get();
    }

    public static void setCurrent(JUnitTestContext context) {
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
