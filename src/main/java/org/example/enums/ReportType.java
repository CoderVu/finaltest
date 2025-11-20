package org.example.enums;

import lombok.Getter;
import org.example.common.Constants;
import org.example.configure.Config;

@Getter
public enum ReportType {
    EXTENT("extent", "com.aventstack.extentreports.ExtentTest", "target/extent");

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
     * @return ReportType, defaults to EXTENT if not found
     */
    public static ReportType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return EXTENT;
        }
        
        String normalized = value.trim().toLowerCase();
        for (ReportType type : values()) {
            if (type.getKey().equals(normalized)) {
                return type;
            }
        }

        return EXTENT;
    }

    /**
     * Get report type from system properties/env/yaml in that order, fallback to EXTENT.
     */
    public static ReportType getConfigured() {
        String value = System.getProperty("reportType");

        if (isEmpty(value)) {
            value = Config.getPropertyOrDefault(Constants.REPORT_TYPE_PROPERTY, null);
        }
        if (isEmpty(value)) {
            value = Config.getPropertyOrDefault("report.strategy", null);
        }
        return fromString(value);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
