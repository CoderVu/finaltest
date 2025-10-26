package org.example.config;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.report.DebugConfig;
import org.example.core.browser.chrome.ChromeBrowserConfig;
import org.example.core.browser.firefox.FirefoxBrowserConfig;
import org.example.core.browser.edge.EdgeBrowserConfig;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
public class BrowserConfig {
    private static final boolean DEFAULT_HEADLESS = false;
    private static final long DEFAULT_TIMEOUT = Constants.DEFAULT_TIMEOUT;
    private static final long DEFAULT_PAGE_LOAD_TIMEOUT = Constants.DEFAULT_PAGE_LOAD_TIMEOUT;

    private static final String BROWSER_PROPERTY = "browser";
    private static final String BASE_URL_PROPERTY = "base.url";
    private static final String HEADLESS_PROPERTY = "headless";
    private static final String TIMEOUT_PROPERTY = "timeout";
    private static final String PAGE_LOAD_TIMEOUT_PROPERTY = "page.load.timeout";
    private static final String ENV_FILE_PROPERTY = "env.file";
    private static final String REMOTE_ENABLED_PROPERTY = "remote.enabled";
    private static final String REMOTE_URL_PROPERTY = "remote.url";
    private static final String W3C_ENABLED_PROPERTY = "w3c.enabled";
    private static final String GRID_ENABLED_PROPERTY = "grid.enabled";

    public static String getBrowser() {
        // Ưu tiên browser từ TestNG parameter (cho parallel execution)
        try {
            if (org.testng.Reporter.getCurrentTestResult() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest() != null) {
                String param = org.testng.Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("browser");
                if (param != null && !param.trim().isEmpty()) {
                    log.info("BrowserConfig: Using TestNG parameter browser: {}", param);
                    return param.trim().toLowerCase();
                }
            }
            
        } catch (Throwable ignored) {
        }
        
        // Kiểm tra xem có single.browser được set không (khi user chọn chỉ 1 browser)
        String singleBrowser = System.getProperty("single.browser");
        if (singleBrowser != null && !singleBrowser.trim().isEmpty()) {
            log.info("BrowserConfig: Using single browser mode: {}", singleBrowser);
            return singleBrowser.trim().toLowerCase();
        }
        
        // Fallback về command line parameter
        String browserFromCmd = System.getProperty(BROWSER_PROPERTY);
        if (browserFromCmd != null && !browserFromCmd.trim().isEmpty()) {
            log.info("BrowserConfig: Using command line browser: {}", browserFromCmd);
            return browserFromCmd.trim().toLowerCase();
        }
        
        // Fallback về default browser từ Constants
        String defaultBrowser = Constants.getDefaultBrowser();
        if (defaultBrowser != null && !defaultBrowser.trim().isEmpty()) {
            log.info("BrowserConfig: Using default browser: {}", defaultBrowser);
            return defaultBrowser.trim().toLowerCase();
        }
        
        // Cuối cùng là Chrome mặc định
        log.info("BrowserConfig: Using fallback browser: chrome");
        return "chrome";
    }

    public static String getBaseUrl() {
        // Only get from system property or Constants, no hardcoding
        String baseUrl = System.getProperty(BASE_URL_PROPERTY);
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return baseUrl.trim();
        }
        return Constants.getBaseUrl();
    }

    public static boolean isHeadless() {
        String headless = System.getProperty(HEADLESS_PROPERTY, String.valueOf(DEFAULT_HEADLESS));
        return Boolean.parseBoolean(headless);
    }

    public static long getTimeout() {
        String timeout = System.getProperty(TIMEOUT_PROPERTY, String.valueOf(DEFAULT_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static long getPageLoadTimeout() {
        String timeout = System.getProperty(PAGE_LOAD_TIMEOUT_PROPERTY, String.valueOf(DEFAULT_PAGE_LOAD_TIMEOUT));
        return Long.parseLong(timeout);
    }

    public static String getEnvFile() {
        // Only get from system property or Constants default, no hardcoding
        String file = System.getProperty(ENV_FILE_PROPERTY);
        if (file != null && !file.trim().isEmpty()) {
            return file.trim();
        }
        return Constants.CONFIG_PROPERTIES_FILE;
    }

    private static String resolveBaseUrl() {
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

    public static boolean isRemoteEnabled() {
        String enabled = System.getProperty(REMOTE_ENABLED_PROPERTY, "false");
        return Boolean.parseBoolean(enabled);
    }

    public static String getRemoteUrl() {
        return System.getProperty(REMOTE_URL_PROPERTY, "");
    }

    public static boolean isW3CEnabled() {
        String enabled = System.getProperty(W3C_ENABLED_PROPERTY, "true");
        return Boolean.parseBoolean(enabled);
    }

    public static boolean isGridEnabled() {
        String enabled = System.getProperty(GRID_ENABLED_PROPERTY, "false");
        return Boolean.parseBoolean(enabled);
    }

    public static void initialize() {
        log.info("Initializing test configuration");
        
        // Setup Allure report directories
        setupAllureDirectories();
        
        // Validate remote configuration
        if (isRemoteEnabled()) {
            validateRemoteConfiguration();
        }
        
        // Interactive configuration if not set via system properties
        if (System.getProperty("interactive.config", "false").equals("true")) {
            InteractiveConfig.configure();
            // Removed TestNGConfigGenerator.generateTestNGConfig(); - using static testng.xml
        }
        
        try {
            Constants.reload(getEnvFile());
        } catch (Exception e) {
            log.warn("Failed to reload Constants with env file: " + getEnvFile() + ". Falling back to defaults.", e);
        }
        log.info("Environment file: " + getEnvFile());
        log.info("Browser: " + getBrowser());
        log.info("Available browsers: " + Constants.getBrowsers());
        log.info("Base URL: " + getBaseUrl());
        log.info("Headless: " + isHeadless());
        log.info("Remote enabled: " + isRemoteEnabled());
        log.info("W3C enabled: " + isW3CEnabled());
        log.info("Grid enabled: " + isGridEnabled());
        if (isRemoteEnabled()) {
            log.info("Remote URL: " + getRemoteUrl());
        }
        log.info("Timeout: " + getTimeout() + "ms");
        log.info("Page Load Timeout: " + getPageLoadTimeout() + "ms");
        initializeSelenide();
    }

    private static void setupAllureDirectories() {
        try {
            // Ensure allure-results directory exists
            Path allureResultsPath = Paths.get("target/allure-results");
            if (!Files.exists(allureResultsPath)) {
                Files.createDirectories(allureResultsPath);
                log.info("Created allure-results directory: {}", allureResultsPath.toAbsolutePath());
            }
            
            // Ensure allure-report directory exists
            Path allureReportPath = Paths.get("target/allure-report");
            if (!Files.exists(allureReportPath)) {
                Files.createDirectories(allureReportPath);
                log.info("Created allure-report directory: {}", allureReportPath.toAbsolutePath());
            }
            
            // Set system properties for Allure
            System.setProperty("allure.results.directory", allureResultsPath.toAbsolutePath().toString());
            System.setProperty("allure.report.directory", allureReportPath.toAbsolutePath().toString());
            
            // Set environment variables (for Allure serve)
            try {
                Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
                java.lang.reflect.Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
                theEnvironmentField.setAccessible(true);
                java.util.Map<String, String> env = (java.util.Map<String, String>) theEnvironmentField.get(null);
                env.put("ALLURE_RESULTS_DIRECTORY", allureResultsPath.toAbsolutePath().toString());
                env.put("ALLURE_REPORT_DIRECTORY", allureReportPath.toAbsolutePath().toString());
            } catch (Exception ignored) {
                log.debug("Could not set environment variables for Allure");
            }
            
            log.info("Allure results directory: {}", allureResultsPath.toAbsolutePath());
            log.info("Allure report directory: {}", allureReportPath.toAbsolutePath());
            
        } catch (Exception e) {
            log.error("Failed to setup Allure directories", e);
        }
    }

    private static void validateRemoteConfiguration() {
        String remoteUrl = getRemoteUrl();
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            throw new IllegalStateException("Remote execution enabled but remote.url is not configured!");
        }
        
        log.info("Validating remote Selenium Grid connection...");
        
        try {
            java.net.URL url = new java.net.URL(remoteUrl + "/status");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.info("✓ Selenium Grid is running at: {}", remoteUrl);
                log.info("✓ Grid UI available at: http://localhost:4444/ui#/");
                log.info("✓ VNC Viewers available:");
                log.info("  - Chrome: http://localhost:7900/");
                log.info("  - Firefox: http://localhost:7901/");
                log.info("  - Edge: http://localhost:7902/");
            } else {
                log.warn("⚠ Selenium Grid returned status code: {}", responseCode);
            }
        } catch (Exception e) {
            log.error("Cannot connect to Selenium Grid at: {}", remoteUrl);
            log.error("Please start Docker services:");
            log.error("1. Run: docker-compose up -d");
            log.error("2. Wait for all services to start");
            log.error("3. Check Grid UI: http://localhost:4444/ui#/");
            log.error("4. Check VNC viewers: http://localhost:7900/, http://localhost:7901/, http://localhost:7902/");
            throw new IllegalStateException("Selenium Grid is not running. Please start Docker services first.", e);
        }
    }

    private static void initializeSelenide() {
        // Use custom WebDriverProvider so each parallel thread can resolve its own browser
        Configuration.browser = org.example.config.GridConfig.class.getName();
        Configuration.headless = isHeadless();
        Configuration.timeout = getTimeout();
        Configuration.pageLoadTimeout = getPageLoadTimeout();
        // Only use resolved base URL from Constants or system property
        Configuration.baseUrl = resolveBaseUrl();
        Configuration.screenshots = false;
        Configuration.savePageSource = false;
        Configuration.fileDownload = com.codeborne.selenide.FileDownloadMode.HTTPGET;
        
        if (!Configuration.browser.equals(org.example.config.GridConfig.class.getName())) {
            configureBrowserOptions();
            if (isRemoteEnabled()) {
                String remote = getRemoteUrl();
                if (remote == null || remote.trim().isEmpty()) {
                    throw new IllegalStateException("remote.enabled=true but remote.url is empty");
                }
                Configuration.remote = remote.trim();
                configureRemoteBrowserCapabilities();
            }
        }
        log.info("Selenide configuration initialized successfully");
    }

    private static void configureBrowserOptions() {
        String browser = getBrowser().toLowerCase();
        switch (browser) {
            case "chrome":
                ChromeBrowserConfig.configure();
                log.info("Chrome options configured");
                break;
            case "firefox":
                FirefoxBrowserConfig.configure();
                log.info("Firefox options configured");
                break;
            case "edge":
                EdgeBrowserConfig.configure();
                log.info("Edge options configured with enhanced settings");
                break;
            default:
                log.warn("Unsupported browser: " + browser + ". Using default configuration.");
                break;
        }
    }

    private static void configureRemoteBrowserCapabilities() {
        String browser = getBrowser().toLowerCase();
        switch (browser) {
            case "chrome":
                Configuration.browserCapabilities.setCapability("browserName", "chrome");
                break;
            case "firefox":
                Configuration.browserCapabilities.setCapability("browserName", "firefox");
                break;
            case "edge":
                Configuration.browserCapabilities.setCapability("browserName", "MicrosoftEdge");
                break;
            default:
                log.warn("Unsupported browser for remote execution: " + browser);
                break;
        }
    }

    public static void setUp() {
        initialize();
        String startUrl = resolveBaseUrl();
        log.info("Opening base URL: " + startUrl);
        Selenide.open(startUrl);
        maximizeWindow();
    }

    public static void tearDown() {
        Selenide.closeWebDriver();
    }
    public static void maximizeWindow() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) {
                log.warn("WebDriver has not been started yet, cannot maximize window");
                return;
            }

            WebDriver driver = WebDriverRunner.getWebDriver();
            driver.manage().window().maximize();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(screenSize.width, screenSize.height));
            log.info("Browser window maximized and set to screen size: " + screenSize.width + "x" + screenSize.height);
        } catch (Exception e) {
            log.error("Error maximizing browser window", e);
        }
    }

    public static boolean isBrowserAvailable(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                return isChromeAvailable();
            case "firefox":
                return isFirefoxAvailable();
            case "edge":
                return isEdgeAvailable();
            default:
                return false;
        }
    }
    
    private static boolean isChromeAvailable() {
        try {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            return true;
        } catch (Exception e) {
            log.debug("Chrome not available: {}", e.getMessage());
            return false;
        }
    }
    
    private static boolean isFirefoxAvailable() {
        try {
            io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
            return true;
        } catch (Exception e) {
            log.debug("Firefox not available: {}", e.getMessage());
            return false;
        }
    }
    
    private static boolean isEdgeAvailable() {
        try {
            io.github.bonigarcia.wdm.WebDriverManager.edgedriver().setup();
            return true;
        } catch (Exception e) {
            log.debug("Edge not available: {}", e.getMessage());
            return false;
        }
    }

    public static boolean shouldSkipBrowser() {
        // Kiểm tra xem có chạy single browser mode không
        String singleBrowserMode = System.getProperty("single.browser");
        if (singleBrowserMode == null || !Boolean.parseBoolean(singleBrowserMode)) {
            // Không phải single browser mode, chạy tất cả
            return false;
        }
        
        // Là single browser mode, kiểm tra browser hiện tại
        String selectedBrowser = System.getProperty("browser");
        String currentBrowser = getBrowser();
        
        if (selectedBrowser != null && !selectedBrowser.trim().isEmpty()) {
            boolean shouldSkip = !selectedBrowser.trim().toLowerCase().equals(currentBrowser.toLowerCase());
            if (shouldSkip) {
                log.info("Skipping browser: {} (Selected: {})", currentBrowser, selectedBrowser);
            }
            return shouldSkip;
        }
        
        return false;
    }
}
