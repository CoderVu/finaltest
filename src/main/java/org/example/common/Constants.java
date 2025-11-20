package org.example.common;

import org.example.enums.BrowserType;

import java.time.Duration;

public class Constants {

    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_TIMESTAMP_REPORT_FORMAT ="yyyyMMdd_HHmmss";

    // ==================== PROPERTY KEYS ====================
    public static final String BASE_URL_PROPERTY = "base_url";
    public static final String BROWSER_PROPERTY = "browser";
    public static final String REPORT_TYPE_PROPERTY = "reportType";
    public static final String REMOTE_URL_PROPERTY = "remote_url";
    public static final String IS_REMOTE_PROPERTY = "isRemote";
    public static final String HEADLESS_PROPERTY = "headless";
    public static final String TIMEOUT_PROPERTY = "timeout";
    public static final String PAGE_LOAD_TIMEOUT_PROPERTY = "page_load_timeout";

    // ==================== DEFAULT VALUES ====================
    public static final String CONFIG_PROPERTIES_FILE = "dev-env.properties";
    public static final String DEFAULT_BROWSER = BrowserType.CHROME.toString();
    public static final String DEFAULT_REPORT = "extent";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    public static final Duration DEFAULT_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(60);
    public static final boolean DEFAULT_HEADLESS = false;
    public static final boolean DEFAULT_REMOTE_ENABLED = false;

    private Constants() {}
}
