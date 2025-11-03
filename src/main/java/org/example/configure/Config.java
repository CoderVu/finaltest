package org.example.configure;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.enums.BrowserType;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Slf4j
public class Config {

    private static Properties envConfig;
    // ThreadLocal cache browser value để tránh log quá nhiều lần và support parallel execution
    private static final ThreadLocal<String> cachedBrowser = new ThreadLocal<>();

    public static void loadEnvironment(String filePath) {
        envConfig = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(filePath)) {
            if (in == null) {
                throw new RuntimeException("Properties file not found in classpath: " + filePath);
            }
            envConfig.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Could not load properties file: " + filePath, e);
        }
    }

    public static String getEnvironmentValue(String key) {
        if (envConfig == null) {
            throw new IllegalStateException("Properties not loaded. Call loadEnvironment() first.");
        }
        return envConfig.getProperty(key);
    }

    public static String getBrowser() {
        // Return cached value nếu đã có (giảm số lần log)
        String cached = cachedBrowser.get();
        if (cached != null) {
            return cached;
        }
        
        List<String> availableBrowsers = getAvailableBrowsers();
        String browser = null;
        
        // Previously there was support for BrowserTestListener to set browser per thread.
        // That listener was removed; we no longer depend on it. Continue with CLI/properties/fallback.
        
        // 2. Ưu tiên thứ 2: CLI/System property (set từ command line: -Dbrowser=chrome)
        // Chỉ dùng nếu BrowserTestListener chưa set (không phải parallel execution từ testng.xml)
        if (browser == null) {
            String browserByCLI = getBrowserByCLI(availableBrowsers);
            if (browserByCLI != null) {
                browser = browserByCLI;
                log.info("Config: Using CLI/System property browser: {} (thread: {})", 
                        browser, Thread.currentThread().getName());
            }
        }
        
        // 3. Ưu tiên thứ 3: Properties file (browser=chrome trong dev-env.properties)
        if (browser == null) {
            String browserByProperties = getBrowserByProperties();
            if (browserByProperties != null && availableBrowsers.contains(browserByProperties)) {
                browser = browserByProperties;
                log.info("Config: Using environment properties browser: {} (thread: {})", 
                        browser, Thread.currentThread().getName());
            } else if (browserByProperties != null) {
                log.warn("Environment properties browser '{}' not in enabled browsers {}. Fallback will apply.", browserByProperties, availableBrowsers);
            }
        }
        
        // 4. Fallback: Browser đầu tiên trong list browsers=chrome,firefox,edge
        if (browser == null) {
            browser = getBrowserFallback(availableBrowsers);
            log.info("Config: Using fallback browser (first in browsers list): {} (thread: {})", 
                    browser, Thread.currentThread().getName());
        }
        
        // Cache browser value per thread
        cachedBrowser.set(browser);
        
        return browser;
    }
    
    /**
     * Reset browser cache cho current thread (dùng khi browser thay đổi trong runtime)
     */
    public static void resetBrowserCache() {
        cachedBrowser.remove();
    }

    /**
     * Set browser for current thread (useful when TestBase reads TestNG parameter and
     * wants Config.getBrowser() to return that value in the same thread).
     */
    public static void setThreadBrowser(String browser) {
        if (browser == null) {
            cachedBrowser.remove();
            return;
        }
        String b = browser.trim().toLowerCase();
        cachedBrowser.set(b);
        log.info("Config: Set thread browser to '{}' (thread: {})", b, Thread.currentThread().getName());
    }

    public static String getBrowserByCLI(List<String> availableBrowsers) {
        String sys = System.getProperty(Constants.BROWSER_PROPERTY);
        if (sys != null && !sys.trim().isEmpty()) {
            String browser = sys.trim().toLowerCase();
            if (availableBrowsers.contains(browser)) {
                log.info("Config: Using CLI/System property browser: {}", browser);
                return browser;
            } else {
                log.warn("CLI/System property browser '{}' not in enabled browsers {}. Fallback will apply.", browser, availableBrowsers);
            }
        }
        return null;
    }

    public static String getBrowserByProperties() {
        try {
            if (envConfig != null) {
                String envBrowser = getEnvironmentValue(Constants.BROWSER_PROPERTY);
                if (envBrowser != null && !envBrowser.trim().isEmpty()) {
                    return envBrowser.trim().toLowerCase();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String getBrowserFallback(List<String> availableBrowsers) {
        String fallback = availableBrowsers.get(0);
        log.info("Config: Using fallback browser (first in browsers list): {}", fallback);
        return fallback;
    }

    private static List<String> getAvailableBrowsers() {
        List<String> availableBrowsers = null;
        try {
            availableBrowsers = Constants.getBrowsers();
        } catch (Exception e) {
            log.warn("Could not load browser list from properties/env: {}", e.getMessage());
        }
        if (availableBrowsers == null || availableBrowsers.isEmpty()) {
            try {
                String defaultBrowser = Constants.getDefaultBrowser();
                availableBrowsers = List.of(defaultBrowser != null ? defaultBrowser : BrowserType.CHROME.toString());
            } catch (Exception e) {
                availableBrowsers = List.of(BrowserType.CHROME.toString());
            }
        }
        return availableBrowsers;
    }

    public static String getBaseUrl() {
        String baseUrl = System.getProperty(Constants.BASE_URL_PROPERTY);
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return baseUrl.trim();
        }
        return Constants.getBaseUrl();
    }

    public static boolean isHeadless() {
        String headless = System.getProperty(Constants.HEADLESS_PROPERTY, String.valueOf(Constants.DEFAULT_HEADLESS));
        return Boolean.parseBoolean(headless);
    }

    public static long getTimeout() {
        String timeout = System.getProperty(Constants.TIMEOUT_PROPERTY, String.valueOf(Constants.DEFAULT_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static long getPageLoadTimeout() {
        String timeout = System.getProperty(Constants.PAGE_LOAD_TIMEOUT_PROPERTY, String.valueOf(Constants.DEFAULT_PAGE_LOAD_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static String getEnvFile() {
        String file = System.getProperty(Constants.ENV_FILE_PROPERTY);
        if (file != null && !file.trim().isEmpty()) {
            return file.trim();
        }
        return Constants.CONFIG_PROPERTIES_FILE;
    }

    public static boolean isRemoteEnabled() {
        String enabled = System.getProperty(Constants.IS_REMOTE_PROPERTY);
        if (enabled == null || enabled.trim().isEmpty()) {
            String remoteEnabledProp = System.getProperty(Constants.REMOTE_ENABLED_PROPERTY);
            if (remoteEnabledProp != null && !remoteEnabledProp.trim().isEmpty()) {
                enabled = remoteEnabledProp;
            }
        }
        if (enabled != null && !enabled.trim().isEmpty()) {
            return Boolean.parseBoolean(enabled);
        }
        return Constants.isRemoteEnabled();
    }

    public static String getRemoteUrl() {
        String url = System.getProperty(Constants.REMOTE_URL_PROPERTY);
        if (url != null && !url.trim().isEmpty()) {
            return url.trim();
        }
        return Constants.getRemoteUrl();
    }

    public static boolean isW3CEnabled() {
        String enabled = System.getProperty(Constants.IS_W3C_PROPERTY);
        if (enabled == null || enabled.trim().isEmpty()) {
            enabled = System.getProperty(Constants.W3C_ENABLED_PROPERTY);
        }
        if (enabled != null && !enabled.trim().isEmpty()) {
            return Boolean.parseBoolean(enabled);
        }
        try {
            if (envConfig != null) {
                String isW3CStr = getEnvironmentValue(Constants.IS_W3C_PROPERTY);
                if (isW3CStr == null || isW3CStr.trim().isEmpty()) {
                    isW3CStr = getEnvironmentValue(Constants.W3C_ENABLED_PROPERTY);
                }
                if (isW3CStr != null && !isW3CStr.trim().isEmpty()) {
                    return Boolean.parseBoolean(isW3CStr.trim());
                }
            }
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean isGridEnabled() {
        String enabled = System.getProperty(Constants.IS_GRID_PROPERTY);
        if (enabled == null || enabled.trim().isEmpty()) {
            enabled = System.getProperty(Constants.GRID_ENABLED_PROPERTY);
        }
        if (enabled != null && !enabled.trim().isEmpty()) {
            return Boolean.parseBoolean(enabled);
        }
        return Constants.isGridEnabled();
    }

    public static boolean shouldSkipBrowser() {
        List<String> availableBrowsers = null;
        try {
            availableBrowsers = Constants.getBrowsers();
        } catch (Exception e) {
            return false;
        }

        if (availableBrowsers == null || availableBrowsers.isEmpty()) {
            return false;
        }

        if (availableBrowsers.size() > 1) {
            return false;
        }

        String currentBrowser = getBrowser();
        String configuredBrowser = availableBrowsers.get(0).toLowerCase();
        boolean shouldSkip = !configuredBrowser.equals(currentBrowser.toLowerCase());
        
        if (shouldSkip) {
            log.info("Skipping browser: {} (Only {} configured in properties)", currentBrowser, configuredBrowser);
        }
        return shouldSkip;
    }

    public static String resolveBaseUrlForSelenium() {
        String url = getBaseUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Base URL is not configured. Please set it in environment file or system property.");
        }
        url = url.trim();
        if (isRemoteEnabled()) {
            if (url.startsWith("http://localhost")) {
                url = url.replaceFirst("http://localhost", "http://host.docker.internal");
            } else if (url.startsWith("http://127.0.0.1")) {
                url = url.replaceFirst("http://127.0.0.1", "http://host.docker.internal");
            }
        }
        return url;
    }

    public static BrowserType getBrowserType() {
        String browser = getBrowser();
        return BrowserType.fromString(browser);
    }
}
