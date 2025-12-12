package org.example.enums;

/**
 * Query enum for TestRail filter options
 */
public enum Query {
    STATUS("Status", "tests:status_id"),
    TESTED_BY("Tested By", "tests:tested_by");

    private final String displayName;
    private final String groupingKey;

    Query(String displayName, String groupingKey) {
        this.displayName = displayName;
        this.groupingKey = groupingKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroupingKey() {
        return groupingKey;
    }
}

