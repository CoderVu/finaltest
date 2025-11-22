package org.example.enums;

import lombok.Getter;

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
    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

}
