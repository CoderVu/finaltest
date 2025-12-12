package org.example.enums;

import lombok.Getter;

/**
 * Test status enum for TestRail
 */
@Getter
public enum TestStatus {
    PASSED("Passed", "1"),
    BLOCKED("Blocked", "2"),
    UNTESTED("Untested", "3"),
    SKIPPED("Skipped", "4"),
    FAILED("Failed", "5"),
    AUTOMATION_PASSED("Automation Passed", "7"),
    AUTOMATION_FAILED("Automation Failed", "8"),
    AUTOMATION_ERROR("Automation Error", "9");

    private final String displayName;
    private final String value;

    TestStatus(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }

    public static TestStatus fromDisplayName(String displayName) {
        for (TestStatus status : values()) {
            if (status.getDisplayName().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown test status: " + displayName);
    }
}

