package org.example.core.driver;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public abstract class AbstractDriverManager implements IDriver {

    protected WebDriver driver;
    protected final BrowserType browserType;

    protected AbstractDriverManager(BrowserType browserType) {
        this.browserType = browserType;
    }

    @Override
    public BrowserType getBrowserType() {
        return browserType;
    }

    @Override
    public void initDriver() {
        if (driver != null) {
            return;
        }
        // Only use remote if isRemote=true AND remote_url is configured
        String isRemoteValue = Config.getEnvValue("isRemote");
        String remoteUrl = Config.getRemoteUrl();
        
        // Check isRemote first - if false, never use remote
        boolean isRemote = isRemoteValue != null && Boolean.parseBoolean(isRemoteValue);
        boolean hasRemoteUrl = remoteUrl != null && !remoteUrl.trim().isEmpty();
        boolean useRemote = isRemote && hasRemoteUrl;
        
        log.info("Driver init for {}: isRemote={}, remoteUrl={}, useRemote={}", 
                this.browserType, isRemoteValue, remoteUrl, useRemote);
        
        if (useRemote) {
            log.warn("Using REMOTE driver for {} with URL: {}", this.browserType, remoteUrl);
            initRemoteDriver();
        } else {
            log.info("Using LOCAL driver for {} (isRemote={}, hasRemoteUrl={})", this.browserType, isRemote, hasRemoteUrl);
            try {
                setupDriverBinary(this.browserType);
                initLocalDriver();
            } catch (SkipException se) {
                throw se;
            } catch (Throwable t) {
                log.error("Failed to init driver for '{}': {}", this.browserType, t.getMessage(), t);
                throw new SkipException("Failed to init driver for '" + this.browserType + "': " + t.getMessage());
            }
        }
    }

    @Override
    public void setupDriverBinary(BrowserType browserType) {
        if (!Boolean.getBoolean("use.wdm")) {
            log.debug("Skipping WebDriverManager setup (use.wdm not set)");
            return;
        }

        try {
            switch (browserType) {
                case CHROME -> WebDriverManager.chromedriver().setup();
                case FIREFOX -> WebDriverManager.firefoxdriver().setup();
                case EDGE -> WebDriverManager.edgedriver().setup();
                default -> log.warn("No WebDriverManager config for: {}", browserType);
            }
        } catch (Exception e) {
            log.warn("WebDriverManager setup failed for {}: {}", browserType, e.getMessage());
        }
    }

    @Override
    public void initRemoteDriver() {
        String remoteUrl = Config.getRemoteUrl();
        if (remoteUrl == null || remoteUrl.isBlank()) {
            throw new IllegalStateException("remote.url is not configured in Config.");
        }

        try {
            URL url = new URL(remoteUrl);
            driver = createRemoteDriver(url, resolveBrowserVersion());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid remote.url: " + remoteUrl, e);
        }
    }

    protected String resolveBrowserVersion() {
        String sys = System.getProperty("browser.version");
        return (sys != null && !sys.trim().isEmpty()) ? sys.trim() : null;
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
            driver = null;
        }
    }
}
