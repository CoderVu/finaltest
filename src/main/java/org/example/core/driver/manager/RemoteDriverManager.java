package org.example.core.driver.manager;

import org.example.enums.BrowserType;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

/**
 * Base class responsible for remote driver initialization.
 */
public abstract class RemoteDriverManager extends LocalDriverManager {

    protected RemoteDriverManager(BrowserType browserType) {
        super(browserType);
    }

    @Override
    public void initRemoteDriver() {
        URL url = getRemoteConnectionURL();
        createRemoteDriver(url);
    }

    @Override
    public RemoteWebDriver createRemoteDriver(URL url) {
        MutableCapabilities options = createRemoteOptions();
        driver = new RemoteWebDriver(url, options);
        return (RemoteWebDriver) driver;
    }

    protected abstract MutableCapabilities createRemoteOptions();
}

