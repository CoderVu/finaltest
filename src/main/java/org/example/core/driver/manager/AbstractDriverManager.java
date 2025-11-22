package org.example.core.driver.manager;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.IDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.SkipException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Slf4j
public abstract class AbstractDriverManager implements IDriverManager {

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
            quitDriver();
        }

        boolean useRemote = isUseRemote();
        log.info("Initializing {} driver in {} mode", browserType, useRemote ? "remote" : "local");
        try {
            if (useRemote) {
                initRemoteDriver();
            } else {
                initLocalDriver();
            }
        } catch (SkipException se) {
            throw se;
        } catch (Throwable t) {
            log.error("Failed to init driver for '{}': {}", this.browserType, t.getMessage(), t);
            throw new SkipException("Failed to init driver for '" + this.browserType + "': " + t.getMessage());
        }
    }

    protected void initLocalDriver() {
        driver = Objects.requireNonNull(
                createLocalDriver(),
                () -> "Local driver initialization returned null for " + browserType
        );
    }

    protected void initRemoteDriver() {
        URL url = getRemoteConnectionURL();
        MutableCapabilities options = Objects.requireNonNull(
                createRemoteOptions(),
                () -> "Remote options must not be null for " + browserType
        );
        driver = new RemoteWebDriver(url, options);
    }

    protected abstract WebDriver createLocalDriver();
    protected abstract MutableCapabilities createRemoteOptions();

    protected boolean isUseRemote() {
        if (!Config.isRemoteEnabled()) {
            return false;
        }
        String remoteUrl = Config.getRemoteUrl();
        return remoteUrl != null && !remoteUrl.trim().isEmpty();
    }

    public boolean isRemoteSession() {
        return driver instanceof RemoteWebDriver;
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
