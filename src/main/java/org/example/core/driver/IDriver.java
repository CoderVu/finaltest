package org.example.core.driver;

import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import java.net.URL;

public interface IDriver {

    void initDriver();
    void initLocalDriver();
    void initRemoteDriver();
    WebDriver createRemoteDriver(URL url, String browserVersion);
    void setupDriverBinary(BrowserType browserType);
    WebDriver getDriver();
    void quitDriver();

    BrowserType getBrowserType();
}
