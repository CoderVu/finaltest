package org.example.engine.junit;

import org.example.core.context.TestContext;

import java.util.HashMap;
import java.util.Map;

public class JUnitTestContext implements TestContext {

    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}
