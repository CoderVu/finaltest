package org.example.enums;

public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE;

    public static BrowserType fromString(String value) {
        switch (value.toLowerCase()) {
            case "chrome": return CHROME;
            case "firefox": return FIREFOX;
            case "edge": return EDGE;
            default: throw new IllegalArgumentException("Unknown browser: " + value);
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
