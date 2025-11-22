package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;
import org.example.enums.Env;
import org.example.utils.EnvUtils;

import java.time.Duration;

@Slf4j
public final class Config {

    private static final Env ACTIVE_ENV = Env.from(Constants.ACTIVE_ENV_NAME);


    private Config() {}

    public static String getBaseUrl() {
        return getProperty(Constants.BASE_URL_PROPERTY);
    }

    public static boolean isRemoteEnabled() {
        boolean remoteFlag = getBooleanPropertyOrDefault(Constants.IS_REMOTE_PROPERTY, Constants.DEFAULT_REMOTE_ENABLED);
        if (!remoteFlag) {
            return false;
        }
        return !getRemoteUrl().isBlank();
    }

    public static String getRemoteUrl() {
        return getProperty(Constants.REMOTE_URL_PROPERTY);
    }

    /**
     * Resolve browser type from optional parameter or configuration.
     *
     * @param browserParameter optional browser parameter (can be null)
     * @return BrowserType resolved from properties or default
     */
    public static BrowserType getBrowserType(String browserParameter) {
        String candidate = (browserParameter != null && !browserParameter.trim().isEmpty())
                ? browserParameter.trim()
                : getPropertyOrDefault(Constants.BROWSER_PROPERTY, Constants.DEFAULT_BROWSER);

        try {
            return BrowserType.fromString(candidate);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid browser '{}', falling back to CHROME", candidate);
            return BrowserType.CHROME;
        }
    }

    public static BrowserType getBrowserType() {
        return getBrowserType(null);
    }

    public static Duration getTimeout() {
     return Duration.ofSeconds(Long.parseLong(
             getPropertyOrDefault(Constants.TIMEOUT_PROPERTY,
                     String.valueOf(Constants.DEFAULT_TIMEOUT.getSeconds()))
     ));
    }
    public static Duration getPageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(
                getPropertyOrDefault(Constants.PAGE_LOAD_TIMEOUT_PROPERTY,
                        String.valueOf(Constants.DEFAULT_PAGE_LOAD_TIMEOUT.getSeconds()))
        ));
    }

    public static boolean isHeadless() {
        return getBooleanPropertyOrDefault(Constants.HEADLESS_PROPERTY, Constants.DEFAULT_HEADLESS);
    }

    public static String getEnvFile() {
        return ACTIVE_ENV + ".properties";
    }

    public static boolean getBooleanPropertyOrDefault(String key, boolean defaultValue) {
        String value = readEnvProperty(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    public static int getIntPropertyOrDefault(String key, int defaultValue) {
        String value = readEnvProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer for property {}: '{}'. Fallback to {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        String value = readEnvProperty(key);
        return value != null ? value.trim() : defaultValue;
    }

    public static String getProperty(String key) {
        String value = readEnvProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required property: " + key + " for env " + ACTIVE_ENV);
        }
        return value.trim();
    }

    private static String readEnvProperty(String key) {
        return EnvUtils.readProperty(ACTIVE_ENV, key);
    }
}
