package org.example.common;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.EnvUtils;
import org.example.enums.BrowserType;

import java.time.Duration;
import java.util.List;

@Slf4j
public class Constants {

    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_TIMESTAMP_REPORT_FORMAT ="yyyyMMdd_HHmmss";

    // ==================== PROPERTY KEYS ====================
    public static final String BASE_URL_PROPERTY = "base_url";
    public static final String BROWSERS_PROPERTY = "browsers";
    public static final String REPORT_TYPE_PROPERTY = "reportType";
    public static final String REMOTE_URL_PROPERTY = "remote_url";
    public static final String IS_REMOTE_PROPERTY = "isRemote";

    // ==================== DEFAULT VALUES ====================
    public static final String CONFIG_PROPERTIES_FILE = "dev-env.properties";
    public static final String DEFAULT_BROWSER = BrowserType.CHROME.toString();
    public static final String DEFAULT_REPORT = "allure";
    public static final boolean DEFAULT_REMOTE_ENABLED = false;
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    public static final boolean DEFAULT_HEADLESS = false;

    static {
        try {
            EnvUtils.loadEnv(CONFIG_PROPERTIES_FILE);
        } catch (Throwable t) {
            log.warn("Initial EnvUtils.loadEnv failed: {}", t.getMessage());
        }
    }

    public static String getBaseUrl() {
        return EnvUtils.getBaseUrl();
    }

    public static List<String> getBrowsers() {
        return EnvUtils.getBrowsers();
    }

    public static String getDefaultBrowser() {
        return EnvUtils.getDefaultBrowser();
    }

    public static List<String> getReportTypes() {
        return EnvUtils.getReportTypes();
    }

    public static boolean isRemoteEnabled() {
        return EnvUtils.isRemoteEnabled();
    }

    public static boolean isGridEnabled() {
        List<String> browsers = getBrowsers();
        return browsers != null && browsers.size() > 1;
    }

    public static String getRemoteUrl() {
        return EnvUtils.getRemoteUrl();
    }

    public static void loadEnvironment(String envFilePath) {
        EnvUtils.loadEnv(envFilePath);
    }

    public static boolean isInitialized() {
        return EnvUtils.isInitialized();
    }

    public static String getEnvValue(String key) {
        return EnvUtils.getEnv(key);
    }

}
