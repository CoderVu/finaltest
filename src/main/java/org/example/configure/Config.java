package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;

import java.time.Duration;
import java.util.List;

@Slf4j
public class Config {

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
        String candidate = (browserParameter != null && !browserParameter.trim().isEmpty())
                ? browserParameter.trim()
                : Constants.getDefaultBrowser();

        try {
            return BrowserType.fromString(candidate);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid browser '{}', falling back to CHROME", candidate);
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
