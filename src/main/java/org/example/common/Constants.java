package org.example.common;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.BrowserType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Constants {

    // ==================== PROPERTY KEYS ====================
    public static final String BROWSER_PROPERTY = "browser";
    public static final String BASE_URL_PROPERTY = "base.url";
    public static final String HEADLESS_PROPERTY = "headless";
    public static final String TIMEOUT_PROPERTY = "timeout";
    public static final String REPORT_TYPE_PROPERTY = "reportType";
    public static final String DEFAULT_REPORT_TYPE = "allure";
    public static final String PAGE_LOAD_TIMEOUT_PROPERTY = "page.load.timeout";
    public static final String ENV_FILE_PROPERTY = "env.file";
    public static final String REMOTE_URL_PROPERTY = "remote.url";
    public static final String REMOTE_ENABLED_PROPERTY = "remote.enabled";
    public static final String IS_REMOTE_PROPERTY = "isRemote";
    public static final String GRID_ENABLED_PROPERTY = "grid.enabled";
    public static final String IS_GRID_PROPERTY = "isGrid";
    public static final String IS_W3C_PROPERTY = "isW3C";
    public static final String W3C_ENABLED_PROPERTY = "w3c.enabled";
    public static final String BROWSERS_PROPERTY = "browsers";

    // ==================== DEFAULT VALUES ====================
    public static final String CONFIG_PROPERTIES_FILE = System.getProperty(ENV_FILE_PROPERTY, "dev-env.properties");
    public static final boolean DEFAULT_HEADLESS = false;
    public static final long DEFAULT_TIMEOUT = 20000;
    public static final long DEFAULT_PAGE_LOAD_TIMEOUT = 30000;
    public static final boolean DEFAULT_REMOTE_ENABLED = false;
    public static final boolean DEFAULT_GRID_ENABLED = false;
    public static final String DEFAULT_BROWSER = BrowserType.CHROME.toString();

    // ==================== RUNTIME VALUES ====================
    private static volatile boolean initialized = false;
    private static volatile String baseUrl;
    private static volatile List<String> browsers;
    private static volatile List<String> reportTypes;
    private static volatile boolean remoteEnabled;
    private static volatile boolean gridEnabled;
    private static volatile String remoteUrl;

    // ==================== PUBLIC GETTERS ====================
    public static String getBaseUrl() {
        ensureInitialized();
        return baseUrl;
    }

    public static List<String> getBrowsers() {
        ensureInitialized();
        return browsers;
    }

    public static String getDefaultBrowser() {
        ensureInitialized();
        return browsers.isEmpty() ? DEFAULT_BROWSER : browsers.get(0);
    }

    public static boolean isRemoteEnabled() {
        ensureInitialized();
        return remoteEnabled;
    }

    public static boolean isGridEnabled() {
        ensureInitialized();
        return gridEnabled;
    }

    public static String getRemoteUrl() {
        ensureInitialized();
        return remoteUrl;
    }

    // ==================== INITIALIZATION ====================
    public static void loadEnvironment(String envFilePath) {
        synchronized (Constants.class) {
            try {
                Config.loadEnvironment(envFilePath);
                baseUrl = getEnv(BASE_URL_PROPERTY)
                        .or(() -> getEnv("base_url"))
                        .orElseThrow(() -> new IllegalStateException("Missing required key: " + BASE_URL_PROPERTY + " or base_url"));

                browsers = parseBrowsers(getEnv(BROWSERS_PROPERTY)
                        .orElseGet(() -> getEnv(BROWSER_PROPERTY).orElse(DEFAULT_BROWSER)));

                reportTypes = getEnv(REPORT_TYPE_PROPERTY)
                        .map(s -> Arrays.stream(s.split("\\s*,\\s*"))
                                .map(String::trim)
                                .filter(str -> !str.isEmpty())
                                .collect(Collectors.toList()))
                        .orElseGet(() -> Arrays.asList(DEFAULT_REPORT_TYPE));

                remoteEnabled = parseBoolean(
                        getEnv(IS_REMOTE_PROPERTY)
                                .or(() -> getEnv(REMOTE_ENABLED_PROPERTY)),
                        DEFAULT_REMOTE_ENABLED
                );

                gridEnabled = parseBoolean(
                        getEnv(IS_GRID_PROPERTY)
                                .or(() -> getEnv(GRID_ENABLED_PROPERTY)),
                        DEFAULT_GRID_ENABLED
                );

                remoteUrl = getEnv(REMOTE_URL_PROPERTY).orElse("");

                autoGenerateTestNGXml(envFilePath);

                initialized = true;

                log.info("""
                        Environment loaded successfully:
                          • Base URL: {}
                          • Browsers: {}
                          . Report Types: {}
                          • Remote Enabled: {}
                          • Grid Enabled: {}
                          • Remote URL: {}
                        """,
                        baseUrl, browsers, reportTypes, remoteEnabled, gridEnabled, remoteUrl
                );

            } catch (Exception e) {
                initialized = false;
                throw new IllegalStateException("❌ Failed to (re)load Constants from: " + envFilePath, e);
            }
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (Constants.class) {
                if (!initialized) {
                    loadEnvironment(Config.getEnvFile());
                }
            }
        }
    }

    // ==================== HELPERS ====================
    private static Optional<String> getEnv(String key) {
        return Optional.ofNullable(Config.getEnvironmentValue(key))
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    private static boolean parseBoolean(Optional<String> value, boolean defaultValue) {
        return value.map(Boolean::parseBoolean).orElse(defaultValue);
    }

    private static List<String> parseBrowsers(String browsersStr) {
        return Arrays.stream(browsersStr.split("\\s*,\\s*"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ==================== UTILITIES ====================
    /** Reload environment without restarting JVM */
    public static void reload() {
        synchronized (Constants.class) {
            initialized = false;
            ensureInitialized();
        }
    }

    /**
     * Tự động generate testng.xml từ browsers list
     * Chỉ generate nếu cần thiết (file không tồn tại hoặc browsers thay đổi)
     */
    private static void autoGenerateTestNGXml(String envFilePath) {
        try {
            // Sử dụng browsers đã load, không load lại environment (tránh vòng lặp)
            if (browsers != null && !browsers.isEmpty()) {
                org.example.core.browser.TestNGXmlGenerator.generateFromBrowsers(browsers);
            } else {
                log.warn("No browsers to generate testng.xml");
            }
        } catch (Exception e) {
            // Log warning nhưng không throw exception để không gián đoạn quá trình load environment
            log.warn("⚠️  Failed to auto-generate testng.xml: {}. You may need to generate it manually.", e.getMessage());
            log.debug("TestNGXmlGenerator error details", e);
        }
    }

    public static List<String> getReportTypes() {
        ensureInitialized();
        return reportTypes;
    }

}
