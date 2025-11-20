package org.example.core.driver;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.example.utils.EnvUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.SkipException;

import java.net.MalformedURLException;
import java.net.URL;

import static org.example.core.element.util.DriverUtils.sanitizeVersion;

@Slf4j
public abstract class AbstractDriverManager implements IDriver {

    protected WebDriver driver;
    protected final BrowserType browserType;

    protected AbstractDriverManager(BrowserType browserType) {
        this.browserType = browserType;
    }
    protected URL getRemoteConnectionURL() {
        String remoteUrl = Config.getRemoteUrl();
        try {
            return new URL(remoteUrl);
        } catch (MalformedURLException e) {
            log.error("Invalid remote URL configured: '{}'", remoteUrl, e);
            throw new IllegalArgumentException("Remote URL is malformed: " + remoteUrl, e);
        }
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
        String isRemoteValue = EnvUtils.getEnv(Constants.IS_REMOTE_PROPERTY);
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
    public void initRemoteDriver() {
        URL url = getRemoteConnectionURL();
        String sys = System.getProperty("browser.version");
        String browserVersion = (sys != null && !sys.trim().isEmpty()) ? sys.trim() : null;
        driver = createRemoteDriver(url, browserVersion);
    }

    @Override
    public WebDriver createRemoteDriver(URL url, String browserVersion) {
        MutableCapabilities options = createRemoteOptions();
        applyBrowserVersion(options, browserVersion);
        driver = new RemoteWebDriver(url, options);
        return driver;
    }

    protected abstract MutableCapabilities createRemoteOptions();

    private void applyBrowserVersion(MutableCapabilities options, String browserVersion) {
        if (browserVersion == null || browserVersion.trim().isEmpty()) {
            return;
        }
        String sanitized = sanitizeVersion(browserVersion);
        if (options instanceof AbstractDriverOptions) {
            ((AbstractDriverOptions<?>) options).setBrowserVersion(sanitized);
        } else {
            options.setCapability("browserVersion", sanitized);
        }
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
