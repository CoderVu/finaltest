package org.example.enums;

import org.example.common.Constants;

public enum ReportType {
    EXTENT(Constants.DEFAULT_REPORT),
    ALLURE("allure"),
    NOOP("noop");

    private final String key;

    ReportType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
