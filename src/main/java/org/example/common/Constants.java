package org.example.common;

import org.example.enums.BrowserType;
import org.example.enums.Env;

import java.time.Duration;

public class Constants {

    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_TIMESTAMP_REPORT_FORMAT ="yyyyMMdd_HHmmss";
    // ==================== ENVIRONMENTS ====================
    public static final String ACTIVE_ENV_NAME = Env.dev.name();

    // ==================== PROPERTY KEYS ====================
    public static final String BASE_URL_PROPERTY = "base_url";
    public static final String REMOTE_URL_PROPERTY = "remote_url";
    public static final String IS_REMOTE_PROPERTY = "isRemote";
    public static final String HEADLESS_PROPERTY = "headless";
    public static final String TIMEOUT_PROPERTY = "timeout";
    public static final String PAGE_LOAD_TIMEOUT_PROPERTY = "page_load_timeout";

     // Separate retry configuration keys for assertions and element actions
     public static final String MAX_NUM_OF_ATTEMPTS_SORTASSERT_PROPERTY = "max_num_of_attempts_sortassert";
     public static final String MAX_NUM_OF_ATTEMPTS_ACTION_PROPERTY = "max_num_of_attempts_action";
    public static final String BROWSERS_PROPERTY = "browsers";

    // ==================== DEFAULT VALUES ====================
    public static final String DEFAULT_BROWSER = BrowserType.CHROME.toString();
    public static final String DEFAULT_REPORT = "extent";
    public static final String DEFAULT_TEST_ENGINE = "testng";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    public static final Duration DEFAULT_PAGE_LOAD_TIMEOUT = Duration.ofSeconds(60);
    public static final boolean DEFAULT_HEADLESS = true;
    public static final boolean DEFAULT_REMOTE_ENABLED = false;

    private Constants() {}
}
