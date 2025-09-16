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

    // Per-browser configuration moved to core/browser/* configurators

    public static void setUp() {
        initialize();
        Selenide.open(getBaseUrl());
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
