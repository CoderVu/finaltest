package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


@Slf4j
public final class EnvUtils {

    private static Properties props = new Properties();
    private static List<String> browsers = List.of();
    private static List<String> reportTypes = List.of();

    private EnvUtils() { }

    public static void loadEnv(String filePath) {
        synchronized (EnvUtils.class) {
            if (!props.isEmpty()) {
                return;
            }
            InputStream in = findResource(filePath);
            if (in == null) {
                throw new IllegalStateException("Properties file not found: " + filePath);
            }
            try (in) {
                props.load(in);

                String browsersStr = props.getProperty(Constants.BROWSERS_PROPERTY, Constants.DEFAULT_BROWSER);
                browsers = splitList(browsersStr);

                String reportsStr = props.getProperty(Constants.REPORT_TYPE_PROPERTY, Constants.DEFAULT_REPORT);
                reportTypes = splitList(reportsStr);

                log.info("Loaded properties from {}", filePath);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load: " + filePath, e);
            }
        }
    }

    private static InputStream findResource(String filePath) {
        // Try classpath first
        InputStream in = EnvUtils.class.getClassLoader().getResourceAsStream(filePath);
        if (in != null) {
            return in;
        }

        java.io.File file = new java.io.File("src/test/resources/" + filePath);
        if (file.exists()) {
            try {
                return new java.io.FileInputStream(file);
            } catch (Exception e) {
                log.warn("Cannot read file: {}", file.getAbsolutePath());
            }
        }
        
        return null;
    }

    public static String getEnv(String key) {
        // Ensure properties are loaded if not already
        if (props.isEmpty()) {
            try {
                loadEnv(Constants.CONFIG_PROPERTIES_FILE);
            } catch (Exception e) {
                log.debug("Auto-load properties failed: {}", e.getMessage());
            }
        }
        
        String value = props.getProperty(key);
        if (value != null) {
            return value.trim();
        }
        value = System.getProperty(key);
        return value != null ? value.trim() : null;
    }

    public static List<String> getBrowsers() {
        return browsers;
    }

    public static String getDefaultBrowser() {
        return browsers.isEmpty() ? Constants.DEFAULT_BROWSER : browsers.get(0);
    }

    public static List<String> getReportTypes() {
        return reportTypes;
    }

    public static boolean isRemoteEnabled() {
        String value = getEnv(Constants.IS_REMOTE_PROPERTY);
        boolean isRemote = value != null && Boolean.parseBoolean(value);
        if (!isRemote) {
            log.debug("isRemoteEnabled: isRemote=false, returning false");
            return false;
        }
        // Only enable remote if remote_url is configured
        String remoteUrl = getRemoteUrl();
        boolean hasRemoteUrl = remoteUrl != null && !remoteUrl.trim().isEmpty();
        log.info("isRemoteEnabled: isRemote={}, remoteUrl={}, result={}", isRemote, remoteUrl, hasRemoteUrl);
        return hasRemoteUrl;
    }

    public static boolean isGridEnabled() {
        // Only enable Grid if: multiple browsers AND isRemote=true AND remote_url configured
        if (browsers.size() <= 1) {
            return false;
        }
        // Check isRemote first
        String isRemoteValue = getEnv(Constants.IS_REMOTE_PROPERTY);
        boolean isRemote = isRemoteValue != null && Boolean.parseBoolean(isRemoteValue);
        if (!isRemote) {
            log.debug("isGridEnabled: browsers={} but isRemote=false, returning false", browsers.size());
            return false;
        }
        // Check remote_url
        String remoteUrl = getRemoteUrl();
        boolean hasRemoteUrl = remoteUrl != null && !remoteUrl.trim().isEmpty();
        log.info("isGridEnabled: browsers={}, isRemote={}, remoteUrl={}, result={}", 
                browsers.size(), isRemote, remoteUrl, hasRemoteUrl);
        return hasRemoteUrl;
    }

    public static String getRemoteUrl() {
        String value = getEnv(Constants.REMOTE_URL_PROPERTY);
        return value != null ? value : "";
    }

    public static String getBaseUrl() {
        String value = getEnv(Constants.BASE_URL_PROPERTY);
        if (value == null) {
            throw new IllegalStateException("Missing: " + Constants.BASE_URL_PROPERTY);
        }
        return value;
    }

    private static List<String> splitList(String s) {
        if (s == null || s.trim().isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : s.split("\\s*,\\s*")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}


