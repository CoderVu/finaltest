package org.example.enums;

import java.util.Locale;

public enum Env {
    dev,
    prod,
    staging;

    private static final Env DEFAULT = dev;

    public static Env from(String candidate) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return DEFAULT;
        }
        try {
            return Env.valueOf(candidate.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported environment value: " + candidate, ex);
        }
    }

    public static Env getDefault() {
        return DEFAULT;
    }
}
