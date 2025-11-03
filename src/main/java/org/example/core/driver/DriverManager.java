package org.example.core.driver;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.example.enums.PlatformOS;
import org.openqa.selenium.WebDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.SkipException;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public abstract class DriverManager {

    // ThreadLocal for each thread to have a separate driver
    private static final ThreadLocal<DriverManager> instance = new ThreadLocal<>();
    protected WebDriver driver;
    protected BrowserType browserType;

    protected DriverManager() {}

    protected abstract void initLocalDriver();

    public static DriverManager getInstance(BrowserType browserType) {
        DriverManager manager = instance.get();
        if (manager == null) {
            synchronized (DriverManager.class) {
                manager = instance.get();
                if (manager == null) {
                    switch (browserType) {
                        case CHROME:
                            manager = new Chrome();
                            break;
                        case FIREFOX:
                            manager = new Firefox();
                            break;
                        case EDGE:
                            manager = new Edge();
                            break;
                        default:
                            throw new RuntimeException("No such browser supported: " + browserType);
                    }
                    manager.browserType = browserType;
                    manager.initDriver();
                    instance.set(manager);
                }
            }
        }
        return manager;
    }

    public abstract void initDriver();

    /**
     * Initialize driver - checks if remote (Grid) is enabled
     */
    protected void initDriverInternal() {
        if (Config.isRemoteEnabled() || Config.isGridEnabled()) {
            initRemoteDriver();
        } else {
            // Ensure WebDriverManager handles driver binary setup for local execution
            try {
                // use instance browserType (not global Config) so parallel/explicit selection works
                setupDriverBinary(this.browserType);
            } catch (Exception e) {
                log.warn("Failed to setup driver binary via WebDriverManager: {}", e.getMessage());
            }

            // IMPORTANT: catch any error during local driver creation and convert to TestNG SkipException
            // so TestNG will mark class as skipped instead of causing noisy suite failures.
            try {
                initLocalDriver();
            } catch (SkipException se) {
                // propagate explicit skips
                throw se;
            } catch (Throwable t) {
                String msg = String.format("Failed to initialize local driver for '%s': %s. "
                        + "Skipping tests for this browser. Try: -Duse.wdm=true, update msedgedriver/msdrivers or run single browser.",
                        this.browserType, t.getMessage());
                log.error(msg, t);
                throw new SkipException(msg);
            }
        }
    }

    /**
     * Setup driver binary via WebDriverManager for given browser
     */
    protected void setupDriverBinary(BrowserType browserType) {
        if (browserType == null) {
            return;
        }

        // Only run WebDriverManager when explicitly enabled to avoid noisy network calls and parsing errors.
        if (!Boolean.getBoolean("use.wdm")) {
            log.debug("Skipping WebDriverManager setup (use.wdm not set) - will rely on Selenium Manager or system drivers");
            return;
        }

        switch (browserType) {
            case CHROME:
                try {
                    WebDriverManager.chromedriver().setup();
                } catch (Exception e) {
                    log.warn("WebDriverManager chromedriver setup failed: {}", e.getMessage());
                }
                break;
            case FIREFOX:
                try {
                    WebDriverManager.firefoxdriver().setup();
                } catch (Exception e) {
                    log.warn("WebDriverManager firefoxdriver setup failed: {}", e.getMessage());
                }
                break;
            case EDGE:
                try {
                    WebDriverManager.edgedriver().setup();
                } catch (Exception e) {
                    log.warn("WebDriverManager edgedriver setup failed: {}", e.getMessage());
                }
                break;
            default:
                log.debug("No WebDriverManager setup implemented for browser: {}", browserType);
        }
    }


    /**
     * Initialize remote driver for Grid
     */
    protected void initRemoteDriver() {
        String remoteUrl = Config.getRemoteUrl();
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            throw new IllegalStateException("Remote URL is not configured. Please set remote.url property.");
        }

        URL url;
        try {
            url = new URL(remoteUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid remote.url: " + remoteUrl, e);
        }

        String browserVersion = resolveBrowserVersion();
        driver = createRemoteDriver(url, browserVersion);
    }

    /**
     * Create remote driver instance - to be implemented by subclasses
     */
    protected abstract WebDriver createRemoteDriver(URL url, String browserVersion);

    /**
     * Resolve browser version from system property or TestNG parameter
     */
    protected String resolveBrowserVersion() {
        try {
            if (org.testng.Reporter.getCurrentTestResult() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext() != null
                    && org.testng.Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest() != null) {
                String param = org.testng.Reporter.getCurrentTestResult()
                        .getTestContext().getCurrentXmlTest().getParameter("browserVersion");
                if (param != null && !param.trim().isEmpty()) {
                    return param.trim();
                }
            }
        } catch (Throwable ignored) {}

        String sys = System.getProperty("browser.version");
        if (sys != null && !sys.trim().isEmpty()) {
            return sys.trim();
        }

        return null;
    }

    /**
     * Resolve platform OS for Remote (Grid) execution
     * - If system property is set, use it
     * - For Grid/Remote: Default to LINUX because Grid nodes usually run on Linux containers
     * - For Local: Auto-detect from OS of the machine running tests
     */
    protected String resolvePlatformName() {
        // Check system property first (allows override)
        String platform = System.getProperty("platform.name");
        if (platform != null && !platform.trim().isEmpty()) {
            try {
                return PlatformOS.fromString(platform).getValue();
            } catch (IllegalArgumentException e) {
                // If invalid, fall through to default logic
            }
        }

        // For Grid/Remote: Default to LINUX because Docker containers run Linux
        // Most Selenium Grid setups use Linux-based containers
        if (Config.isRemoteEnabled() || Config.isGridEnabled()) {
            return PlatformOS.LINUX.getValue();
        }

        // For Local: Auto-detect from OS of the machine running tests
        return PlatformOS.detectFromOS().getValue();
    }

    public WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("Driver is not initialized. Call initDriver() first.");
        }
        return driver;
    }

    public synchronized void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Ignore exceptions during quit
            } finally {
                driver = null;
            }
        }
    }

    /**
     * Reset instance for current thread
     */
    public static void resetInstance() {
        DriverManager manager = instance.get();
        if (manager != null) {
            manager.quitDriver();
            instance.remove();
        }
    }

    /**
     * Reset all instances (cleanup for all threads)
     */
    public static void resetAllInstances() {
        instance.remove();
    }

    /**
     * Create WebDriver instance using DriverManager
     */
    public static WebDriver createWebDriver() {
        BrowserType browserType = Config.getBrowserType();
        return getInstance(browserType).getDriver();
    }

    /**
     * Quit WebDriver and reset instance
     */
    public static void quitWebDriver() {
        BrowserType browserType = Config.getBrowserType();
        getInstance(browserType).quitDriver();
        resetInstance();
    }

    /**
     * Initialize WebDriver and open base URL using explicitly provided BrowserType.
     * Use this from TestBase/@BeforeClass where browser is read from ITestContext or @Parameters.
     */
    public static void initializeDriver(BrowserType browserType) {
        if (browserType == null) {
            browserType = Config.getBrowserType(); // fallback to existing behavior
        }
        WebDriver driver = getInstance(browserType).getDriver();
        String baseUrl = Config.resolveBaseUrlForSelenium();
        log.info("Opening base URL: {}", baseUrl);
        driver.get(baseUrl);
    }

    public static void maximizeWindow() {
        BrowserType browserType = Config.getBrowserType();
        WebDriver driver = getInstance(browserType).getDriver();
        try {
            driver.manage().window().maximize();
            log.info("Browser window maximized.");
        } catch (Exception e) {
            log.warn("Failed to maximize browser window: {}", e.getMessage());
        }
    }

    /**
     * Create WebDriver instance using explicit BrowserType (avoids reading System properties).
     */
    public static WebDriver createWebDriver(BrowserType browserType) {
        if (browserType == null) {
            browserType = Config.getBrowserType();
        }
        return getInstance(browserType).getDriver();
    }

    /**
     * Quit WebDriver and reset instance for explicit BrowserType
     */
    public static void quitWebDriver(BrowserType browserType) {
        if (browserType == null) {
            browserType = Config.getBrowserType();
        }
        getInstance(browserType).quitDriver();
        resetInstance();
    }
}
