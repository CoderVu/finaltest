package org.example.configure;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;


@Slf4j
public class BrowserConfig {
    // moved to ExecutionConfig to enforce SRP

    public static void configureChromeOptions() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("goog:chromeOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--remote-allow-origins=*",
                                "--disable-web-security",
                                "--disable-features=VizDisplayCompositor"
                        ),
                        "excludeSwitches", Arrays.asList("enable-automation"),
                        "useAutomationExtension", false
                )
        );
    }

    public static void configureEdgeOptions() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("ms:edgeOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--disable-web-security",
                                "--disable-features=VizDisplayCompositor",
                                "--disable-extensions",
                                "--disable-plugins",
                                "--disable-images",
                                "--disable-javascript",
                                "--no-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu",
                                "--remote-debugging-port=9222"
                        ),
                        "excludeSwitches", Arrays.asList("enable-automation", "enable-logging"),
                        "useAutomationExtension", false,
                        "detach", true
                )
        );
        Configuration.browserCapabilities.setCapability("platformName", "windows");
        Configuration.browserCapabilities.setCapability("ms:edgeChromium", true);
    }

    public static void configureFirefoxOptions() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("moz:firefoxOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--disable-web-security"
                        ),
                        "prefs", Map.of(
                                "dom.webdriver.enabled", false,
                                "useAutomationExtension", false
                        )
                )
        );
    }

    public static String getBrowser() {
        return ExecutionConfig.getBrowser();
    }

    public static String getBaseUrl() {
        return ExecutionConfig.getBaseUrl();
    }

    public static boolean isHeadless() {
        return ExecutionConfig.isHeadless();
    }

    public static long getTimeout() {
        return ExecutionConfig.getTimeout();
    }

    public static long getPageLoadTimeout() {
        return ExecutionConfig.getPageLoadTimeout();
    }

    public static String getEnvFile() {
        return ExecutionConfig.getEnvFile();
    }

    private static String resolveBaseUrl() {
        return ExecutionConfig.resolveBaseUrlForSelenide();
    }

    public static boolean isRemoteEnabled() {
        return ExecutionConfig.isRemoteEnabled();
    }

    public static String getRemoteUrl() {
        return ExecutionConfig.getRemoteUrl();
    }

    public static boolean isW3CEnabled() {
        return ExecutionConfig.isW3CEnabled();
    }

    public static boolean isGridEnabled() {
        return ExecutionConfig.isGridEnabled();
    }

    public static void initialize() {
        log.info("Initializing test configuration");

        // Allure directories now handled by org.example.report.AllureConfig (@BeforeSuite)

        // Validate remote configuration
        if (isRemoteEnabled()) {
            validateRemoteConfiguration();
        }

        // Interactive configuration if not set via system properties
        if (System.getProperty("interactive.config", "false").equals("true")) {
            ExecutionConfig.configureInteractive();
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
        initializeSelenium();
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

    private static void initializeSelenium() {

        Configuration.browser = org.example.configure.GridConfig.class.getName();
        Configuration.headless = isHeadless();
        Configuration.timeout = getTimeout();
        Configuration.pageLoadTimeout = getPageLoadTimeout();

        Configuration.baseUrl = resolveBaseUrl();
        Configuration.screenshots = false;
        Configuration.savePageSource = false;
        Configuration.fileDownload = com.codeborne.selenide.FileDownloadMode.HTTPGET;

        if (!Configuration.browser.equals(org.example.configure.GridConfig.class.getName())) {
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
                configureChromeOptions();
                log.info("Chrome options configured");
                break;
            case "firefox":
                configureFirefoxOptions();
                log.info("Firefox options configured");
                break;
            case "edge":
                configureEdgeOptions();
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
        return ExecutionConfig.shouldSkipBrowser();
    }
}
