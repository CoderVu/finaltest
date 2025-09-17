package org.example.config;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.report.LogConfig;
import org.example.core.browser.chrome.ChromeBrowserConfig;
import org.example.core.browser.firefox.FirefoxBrowserConfig;
import org.example.core.browser.edge.EdgeBrowserConfig;
import org.openqa.selenium.WebDriver;

import java.awt.*;


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
                    System.out.println("BrowserConfig: Using TestNG parameter browser: " + param);
                    return param.trim().toLowerCase();
                }
            }
        } catch (Throwable ignored) {
        }
        
        // Fallback về command line parameter
        String browserFromCmd = System.getProperty(BROWSER_PROPERTY);
        if (browserFromCmd != null && !browserFromCmd.trim().isEmpty()) {
            System.out.println("BrowserConfig: Using command line browser: " + browserFromCmd);
            return browserFromCmd.trim().toLowerCase();
        }
        
        // Fallback về default browser từ Constants
        String defaultBrowser = Constants.getDefaultBrowser();
        if (defaultBrowser != null && !defaultBrowser.trim().isEmpty()) {
            System.out.println("BrowserConfig: Using default browser: " + defaultBrowser);
            return defaultBrowser.trim().toLowerCase();
        }
        
        // Cuối cùng là Chrome mặc định
        System.out.println("BrowserConfig: Using fallback browser: chrome");
        return "chrome";
    }

    public static String getBaseUrl() {
        return System.getProperty(BASE_URL_PROPERTY, Constants.getBaseUrl());
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
        String file = System.getProperty(ENV_FILE_PROPERTY, "dev-env.yaml");
        if (file == null || file.trim().isEmpty()) {
            return "dev-env.yaml";
        }
        return file;
    }

    private static String resolveBaseUrl() {
        String raw = getBaseUrl();
        if (raw == null || raw.trim().isEmpty()) {
            raw = "https://tiki.vn";
        }
        String url = raw.trim();
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

    private static void initializeSelenide() {
        // Use custom WebDriverProvider so each parallel thread can resolve its own browser
        Configuration.browser = org.example.config.GridWDPConfig.class.getName();
        Configuration.headless = isHeadless();
        Configuration.timeout = getTimeout();
        Configuration.pageLoadTimeout = getPageLoadTimeout();
        // Keep baseUrl consistent with what we open
        Configuration.baseUrl = resolveBaseUrl();
        Configuration.screenshots = false;
        Configuration.savePageSource = false;
        Configuration.fileDownload = com.codeborne.selenide.FileDownloadMode.HTTPGET;
        Configuration.reportsFolder = "target/selenide-reports";
        // When using a custom provider, options/capabilities are configured inside the provider
        // Keep legacy path only if not using provider (backward compatibility)
        if (!Configuration.browser.equals(org.example.config.GridWDPConfig.class.getName())) {
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
        try {
            WebDriver current = WebDriverRunner.getWebDriver();
            WebDriver decorated = LogConfig.decorate(current);
            WebDriverRunner.setWebDriver(decorated);
        } catch (Exception e) {
            log.warn("Failed to register LogConfig WebDriver listener: {}", e.getMessage());
        }
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
}
