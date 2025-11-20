package org.example.core.driver.manager;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.IDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

import java.net.MalformedURLException;
import java.net.URL;

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
            return;
        }
        boolean useRemote = shouldUseRemote();
        log.info("Driver init for {}: isRemote={}, remoteUrl={}, useRemote={}",
                this.browserType,
                Config.isRemoteEnabled(),
                Config.getRemoteUrl(),
                useRemote);

        if (useRemote) {
            log.warn("Using REMOTE driver for {} with URL: {}", this.browserType,
                    Config.getRemoteUrl());
            initRemoteDriver();
        } else {
            log.info("Using LOCAL driver for {}", this.browserType);
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
    
    protected boolean shouldUseRemote() {
        if (!Config.isRemoteEnabled()) {
            return false;
        }
        String remoteUrl = Config.getRemoteUrl();
        return remoteUrl != null && !remoteUrl.trim().isEmpty();
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
