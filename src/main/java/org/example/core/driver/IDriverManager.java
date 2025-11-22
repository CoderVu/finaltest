package org.example.core.driver;

import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;

public interface IDriverManager {

    void initDriver();
    WebDriver getDriver();
    void quitDriver();
    BrowserType getBrowserType();
}
