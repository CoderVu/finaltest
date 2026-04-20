package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;
import org.example.enums.Env;
import org.example.enums.TestEngine;
import org.example.utils.EnvUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (browserParameter != null && !browserParameter.trim().isEmpty()) {
            try {
                return BrowserType.fromString(browserParameter.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid browser parameter '{}', falling back to configured browsers", browserParameter);
            }
        }

        List<BrowserType> configured = getBrowserTypes();
        return configured.isEmpty() ? BrowserType.fromString(Constants.DEFAULT_BROWSER) : configured.get(0);
    }

    /**
     * Returns the list of BrowserTypes specified by the 'browsers' property.
     * If the property is missing or invalid, returns a single-element list with DEFAULT_BROWSER.
     */
    public static List<BrowserType> getBrowserTypes() {
        String raw = getProperty(Constants.BROWSERS_PROPERTY, Constants.DEFAULT_BROWSER);
        if (raw == null || raw.trim().isEmpty()) {
            return List.of(BrowserType.fromString(Constants.DEFAULT_BROWSER));
        }
        List<BrowserType> result = new ArrayList<>();
        Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(s -> {
                    try {
                        result.add(BrowserType.fromString(s));
                    } catch (IllegalArgumentException e) {
                        log.warn("Ignoring unknown browser '{}' in property '{}'", s, Constants.BROWSERS_PROPERTY);
                    }
                });
        if (result.isEmpty()) {
            result.add(BrowserType.fromString(Constants.DEFAULT_BROWSER));
        }
        return result;
    }

    public static Duration getTimeout() {
     return Duration.ofSeconds(Long.parseLong(
             getProperty(Constants.TIMEOUT_PROPERTY,
                     String.valueOf(Constants.DEFAULT_TIMEOUT.getSeconds()))
     ));
    }
    public static Duration getPageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(
                getProperty(Constants.PAGE_LOAD_TIMEOUT_PROPERTY,
                        String.valueOf(Constants.DEFAULT_PAGE_LOAD_TIMEOUT.getSeconds()))
        ));
    }

    public static boolean isHeadless() {
        return getBooleanPropertyOrDefault(Constants.HEADLESS_PROPERTY, Constants.DEFAULT_HEADLESS);
    }


    public static String getEnvFile() {
        return ACTIVE_ENV + ".properties";
    }

    public static TestEngine getTestEngine() {
        return TestEngine.from(Constants.DEFAULT_TEST_ENGINE);
    }

    public static boolean getBooleanPropertyOrDefault(String key, boolean defaultValue) {
        String value = readEnvProperty(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    public static int getIntProperty(String key, int defaultValue) {
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

    public static String getProperty(String key, String defaultValue) {
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
    public static int getMaxActionRetries() {
        return Config.getIntProperty(Constants.MAX_NUM_OF_ATTEMPTS_ACTION_PROPERTY, 3);
    }

    public static int getMaxAttempts() {
        return Config.getIntProperty(Constants.MAX_NUM_OF_ATTEMPTS_ACTION_PROPERTY, 5);
    }
}
