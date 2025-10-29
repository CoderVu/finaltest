package org.example.enums;

import lombok.Getter;

/**
 * Enum for supported report types.
 * Used to avoid hardcoded strings and improve type safety.
 */
@Getter
public enum ReportType {
    ALLURE("allure", "io.qameta.allure.Allure", "target/allure-results"),
    EXTENT("extent", "com.aventstack.extentreports.ExtentTest", "target/extent"),
    JENKINS("jenkins", "org.testng.Reporter", "target/surefire-reports");

    private final String key;
    private final String library;
    private final String outputPath;

    ReportType(String key, String library, String outputPath) {
        this.key = key;
        this.library = library;
        this.outputPath = outputPath;
    }

    /**
     * Parse string to ReportType enum.
     * 
     * @param value string value (case-insensitive)
     * @return ReportType, defaults to ALLURE if not found
     */
    public static ReportType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ALLURE;
        }
        
        String normalized = value.trim().toLowerCase();
        for (ReportType type : values()) {
            if (type.getKey().equals(normalized)) {
                return type;
            }
        }
        
        // Fallback aliases
        if (normalized.contains("extent")) {
            return EXTENT;
        }
        if (normalized.contains("jenkins")) {
            return JENKINS;
        }
        
        return ALLURE; // default
    }

    /**
     * Get report type from system properties.
     * Checks both system property and environment variable.
     */
    public static ReportType getConfigured() {
        String value = System.getProperty("report.strategy", System.getenv("REPORT_STRATEGY"));
        return fromString(value);
    }

    /**
     * Check if current configured type matches this enum.
     */
    public boolean isConfigured() {
        return getConfigured() == this;
    }
}
