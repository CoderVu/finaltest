package org.example.core.browser.chrome;

import org.example.core.driver.manager.LocalDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class LocalChromeDriver extends LocalDriverManager {

    @Override
    public void createWebDriver(String key) throws Exception {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions ops = new ChromeOptions();
            ops.merge(getCapabilities());
            ops.addArguments(getArguments());
            WebDriver webDriver = new ChromeDriver(ops);
            webDriver.manage().window().maximize();
            this.webDrivers.put(key, webDriver);
        } catch (Exception ex) {
            System.out.println("Getting error when creating Web Driver: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
    }
}
