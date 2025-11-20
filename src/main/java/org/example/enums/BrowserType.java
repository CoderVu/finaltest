package org.example.enums;

import java.util.Arrays;

public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE;

    public static BrowserType fromString(String value) {
        for (BrowserType type : BrowserType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown browser: " + value);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
