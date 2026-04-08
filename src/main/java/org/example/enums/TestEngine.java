package org.example.enums;

import java.util.Locale;

public enum TestEngine {
    TESTNG,
    JUNIT;

    private static final TestEngine DEFAULT = TESTNG;

    public static TestEngine from(String candidate) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return DEFAULT;
        }
        try {
            return TestEngine.valueOf(candidate.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported test engine value: " + candidate, ex);
        }
    }
}
