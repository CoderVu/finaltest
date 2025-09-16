package org.example.common;

import org.example.config.EnvConfig;

public class Constants {

    public static final String CONFIG_PROPERTIES_FILE = System.getProperty("env.file", "dev-env.yaml");
    private static volatile boolean initialized = false;
    private static volatile String baseUrl;
    private static volatile String defaultBrowser;
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

    public static void reload() {
        reload(CONFIG_PROPERTIES_FILE);
    }

    public static void reload(String envFilePath) {
        synchronized (Constants.class) {
            try {
                EnvConfig.load(envFilePath);
                baseUrl = EnvConfig.get("base_url");
                defaultBrowser = EnvConfig.get("browser");
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