package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;
import org.example.utils.EnvUtils;

import java.time.Duration;
import java.util.List;

@Slf4j
public class Config {

    public static String getEnvValue(String key) {
        return EnvUtils.getEnv(key);
    }

    public static String getBaseUrl() {
        return Constants.getBaseUrl();
    }

    public static List<String> getBrowsers() {
        return Constants.getBrowsers();
    }

    public static List<String> getReportTypes() {
        return Constants.getReportTypes();
    }

    public static boolean isRemoteEnabled() {
        return Constants.isRemoteEnabled();
    }

    public static String getRemoteUrl() {
        return Constants.getRemoteUrl();
    }

    public static boolean isGridEnabled() {
        return Constants.isGridEnabled();
    }

    /**
     * Get browser type from properties file
     *
     * @param browserParameter optional browser parameter (can be null)
     * @return BrowserType resolved from properties or default
     */
    public static BrowserType getBrowserType(String browserParameter) {
        if (browserParameter != null && !browserParameter.trim().isEmpty()) {
            try {
                return BrowserType.fromString(browserParameter.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid browser parameter '{}', falling back to configured default", browserParameter);
            }
        }
        String browserStr = Constants.getDefaultBrowser();
        try {
            return BrowserType.fromString(browserStr);
        } catch (IllegalArgumentException e) {
            log.warn("Configured browser '{}' is invalid, falling back to CHROME", browserStr);
            return BrowserType.CHROME;
        }
    }

    public static BrowserType getBrowserType() {
        return getBrowserType((String) null);
    }

    public static Duration getTimeout() {
        return Constants.DEFAULT_TIMEOUT;
    }

    public static boolean isHeadless() {
        return Constants.DEFAULT_HEADLESS;
    }

    public static String getEnvFile() {
        return Constants.CONFIG_PROPERTIES_FILE;
    }

}
