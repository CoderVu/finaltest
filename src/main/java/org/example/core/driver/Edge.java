package org.example.core.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.PlatformOS;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

@Slf4j
public class Edge extends DriverManager {
    @Override
    public void initDriver() {
        initDriverInternal();
    }

    @Override
    protected void initLocalDriver() {
        EdgeOptions options = buildEdgeOptions();

        boolean initialUseWdm = Boolean.getBoolean("use.wdm");
        if (!tryCreateWithWdm(initialUseWdm, options)) {
            log.warn("Initial Edge driver creation failed using use.wdm={}, retrying with opposite strategy", initialUseWdm);
            if (!tryCreateWithWdm(!initialUseWdm, options)) {
                throw new RuntimeException("Failed to initialize EdgeDriver (both WDM and SeleniumManager strategies failed)");
            }
        }
        try {
            if (driver instanceof HasCapabilities) {
                String actualVersion = ((HasCapabilities) driver).getCapabilities().getBrowserVersion();
                if (actualVersion != null) {
                    log.info("✅ Edge driver initialized with browser version: {}", actualVersion);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get browser version from driver: {}", e.getMessage());
        }
    }

    private boolean tryCreateWithWdm(boolean useWdm, EdgeOptions options) {
        try {
            if (useWdm) {
                try {
                    WebDriverManager.edgedriver().setup();
                    log.debug("WebDriverManager: edgedriver.setup() completed (useWdm=true)");
                } catch (Exception e) {
                    // Log and continue; we still attempt creation (Selenium Manager may handle it)
                    log.warn("WebDriverManager.edgedriver().setup() failed: {} (will still attempt driver creation)", e.getMessage());
                }
            } else {
                log.debug("Skipping WebDriverManager for Edge (useWdm=false), letting Selenium Manager handle driver");
            }

            driver = new EdgeDriver(options);
            return true;
        } catch (SessionNotCreatedException snce) {
            log.warn("SessionNotCreatedException creating EdgeDriver (useWdm={}): {}", useWdm, snce.getMessage());
            safeQuitDriver();
            return false;
        } catch (Throwable t) {
            log.warn("Exception creating EdgeDriver (useWdm={}): {}", useWdm, t.getMessage());
            safeQuitDriver();
            return false;
        }
    }

    private void safeQuitDriver() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception ignored) {}
        driver = null;
    }

    @Override
    protected WebDriver createRemoteDriver(URL url, String browserVersion) {
        EdgeOptions options = buildRemoteEdgeOptions(browserVersion);
        return new RemoteWebDriver(url, options);
    }

    private EdgeOptions buildEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.setCapability("platformName", PlatformOS.detectFromOS().getValue());
        options.setCapability("ms:edgeChromium", true);
        
        options.addArguments(
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
        );

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        
        return options;
    }

    private EdgeOptions buildRemoteEdgeOptions(String browserVersion) {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("browserName", "MicrosoftEdge");
        if (browserVersion != null) {
            options.setCapability("browserVersion", browserVersion);
        }
        options.setCapability("platformName", resolvePlatformName());
        options.setCapability("acceptInsecureCerts", true);
        
        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        
        return options;
    }
}

