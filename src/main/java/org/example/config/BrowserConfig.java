package org.example.config;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.Constants;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;


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

    public static String getBrowser() {
        return System.getProperty(BROWSER_PROPERTY, Constants.getDefaultBrowser());
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
        return System.getProperty(ENV_FILE_PROPERTY, "dev-env.yaml");
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
        log.info("Base URL: " + getBaseUrl());
        log.info("Headless: " + isHeadless());
        log.info("Timeout: " + getTimeout() + "ms");
        log.info("Page Load Timeout: " + getPageLoadTimeout() + "ms");
        initializeSelenide();
    }

    private static void initializeSelenide() {
        Configuration.browser = getBrowser();
        Configuration.headless = isHeadless();
        Configuration.timeout = getTimeout();
        Configuration.pageLoadTimeout = getPageLoadTimeout();
        Configuration.screenshots = false;
        Configuration.savePageSource = false;
        Configuration.fileDownload = com.codeborne.selenide.FileDownloadMode.HTTPGET;
        Configuration.reportsFolder = "target/selenide-reports";
        configureBrowserOptions();
        log.info("Selenide configuration initialized successfully");
    }

    private static void configureBrowserOptions() {
        String browser = getBrowser().toLowerCase();
        switch (browser) {
            case "chrome":
                configureChromeOptions();
                break;
            case "firefox":
                configureFirefoxOptions();
                break;
            case "edge":
                configureEdgeOptions();
                break;
            default:
                log.warn("Unsupported browser: " + browser + ". Using default configuration.");
                break;
        }
    }

    private static void configureChromeOptions() {
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
        log.info("Chrome options configured");
    }

    private static void configureFirefoxOptions() {
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
        log.info("Firefox options configured");
    }

    private static void configureEdgeOptions() {
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
        log.info("Edge options configured with enhanced settings");
    }

    public static void setUp() {
        initialize();
        Selenide.open(getBaseUrl());
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
