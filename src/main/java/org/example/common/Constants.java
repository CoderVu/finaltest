package org.example.common;

import org.example.config.EnvConfig;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String CONFIG_PROPERTIES_FILE = System.getProperty("env.file", "dev-env.yaml");
    private static volatile boolean initialized = false;
    private static volatile String baseUrl;
    private static volatile String defaultBrowser;
    private static volatile List<String> browsers;
    public static final long DEFAULT_TIMEOUT = 20000;
    public static final long DEFAULT_PAGE_LOAD_TIMEOUT = 30000;

    public static String getBaseUrl() {
        ensureInitialized();
        return baseUrl;
    }

    public static String getDefaultBrowser() {
        ensureInitialized();
        return defaultBrowser;
    }

    public static List<String> getBrowsers() {
        ensureInitialized();
        
        return browsers;
    }

    public static void reload() {
        reload(CONFIG_PROPERTIES_FILE);
    }

    public static void reload(String envFilePath) {
        synchronized (Constants.class) {
            try {
                EnvConfig.load(envFilePath);
                baseUrl = EnvConfig.get("base_url");
                
                // Validate that base_url is configured
                if (baseUrl == null || baseUrl.trim().isEmpty()) {
                    throw new IllegalStateException("base_url is not configured in environment file: " + envFilePath);
                }
                
                // Try to get browsers list first, fallback to single browser
                String browsersStr = EnvConfig.get("browsers");
                if (browsersStr != null && !browsersStr.trim().isEmpty()) {
                    // Parse browsers list from YAML
                    browsersStr = browsersStr.replaceAll("\\[|\\]", "").trim();
                    browsers = Arrays.asList(browsersStr.split("\\s*,\\s*"));
                    defaultBrowser = browsers.get(0); // First browser as default
                } else {
                    // Fallback to single browser
                    defaultBrowser = EnvConfig.get("browser");
                    if (defaultBrowser == null || defaultBrowser.trim().isEmpty()) {
                        throw new IllegalStateException("Neither 'browsers' nor 'browser' is configured in environment file: " + envFilePath);
                    }
                    browsers = Arrays.asList(defaultBrowser);
                }
                
                initialized = true;
            } catch (Exception e) {
                initialized = false;
                throw new IllegalStateException("Failed to (re)load Constants with file: " + envFilePath, e);
            }
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (Constants.class) {
                if (!initialized) {
                    reload(CONFIG_PROPERTIES_FILE);
                }
            }
        }
    }
}