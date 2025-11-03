package org.example.enums;

public enum PlatformOS {
    WINDOWS("windows"),
    LINUX("linux"),
    MAC("mac");

    private final String value;

    PlatformOS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PlatformOS fromString(String platformName) {
        if (platformName == null) {
            return null;
        }
        String normalized = platformName.trim().toLowerCase();
        for (PlatformOS platform : PlatformOS.values()) {
            if (platform.value.equals(normalized)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + platformName);
    }

    public static PlatformOS detectFromOS() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return WINDOWS;
        } else if (osName.contains("mac")) {
            return MAC;
        } else {
            return LINUX;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

