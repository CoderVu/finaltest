package org.example.enums;

import lombok.Getter;
import org.example.configure.Config;

@Getter
public enum ReportType {
    ALLURE("allure", "io.qameta.allure.Allure", "configured in pom.xml"),
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

        if (normalized.contains("extent")) {
            return EXTENT;
        }
        
        return ALLURE;
    }

    /**
     * Get report type from system properties/env/yaml in that order, fallback to ALLURE.
     */
    public static ReportType getConfigured() {
        String value = System.getProperty("reportType");

        if (isEmpty(value)) {
            try { value = Config.getEnvValue("reportType"); } catch (Throwable ignored) { }
        }
        if (isEmpty(value)) {
            try { value = Config.getEnvValue("report.strategy"); } catch (Throwable ignored) { }
        }
        return fromString(value);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
