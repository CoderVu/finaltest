package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;
import org.example.utils.EnvUtils;

import java.time.Duration;

@Slf4j
public final class Config {

    private Config() {}

    private static String loadedEnvFile = Constants.CONFIG_PROPERTIES_FILE;

    static {
        EnvUtils.load(loadedEnvFile);
    }

    public static String getBaseUrl() {
        return getProperty(Constants.BASE_URL_PROPERTY);
    }

    public static boolean isRemoteEnabled() {
        boolean remoteFlag = getBoolean(Constants.IS_REMOTE_PROPERTY, Constants.DEFAULT_REMOTE_ENABLED);
        if (!remoteFlag) {
            return false;
        }
        return !getRemoteUrl().isBlank();
    }

    public static String getRemoteUrl() {
        return getPropertyOrDefault(Constants.REMOTE_URL_PROPERTY, "");
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
        return getBoolean(Constants.HEADLESS_PROPERTY, Constants.DEFAULT_HEADLESS);
    }

    public static String getEnvFile() {
        return loadedEnvFile;
    }



    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = EnvUtils.get(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        String value = EnvUtils.get(key);
        return value != null ? value.trim() : defaultValue;
    }

    public static String getProperty(String key) {
        String value = EnvUtils.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }
}
