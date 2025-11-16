package org.example.enums;

public enum BrowserType {
    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("edge");

    private final String value;

    BrowserType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static BrowserType fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Browser value is null");
        }
        String normalized = input.trim().toLowerCase();
        for (BrowserType t : values()) {
            if (t.value.equalsIgnoreCase(normalized) || t.name().equalsIgnoreCase(normalized)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown browser: " + input);
    }

    @Override
    public String toString() {
        return value;
    }
}
