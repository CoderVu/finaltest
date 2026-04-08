package org.example.utils;

import java.util.Locale;

public final class NormalizeUtils {

    private NormalizeUtils() {}

    public static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }
}
