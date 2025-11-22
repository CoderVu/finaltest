package org.example.core.driver;

import org.example.configure.Config;
import org.example.core.driver.manager.AbstractDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Firefox extends AbstractDriverManager {

    public Firefox() {
        super(BrowserType.FIREFOX);
    }

    @Override
    protected WebDriver createLocalDriver() {
        return new FirefoxDriver(buildFirefoxOptions());
    }

    @Override
    protected FirefoxOptions createRemoteOptions() {
        return buildFirefoxOptions();
    }

    public static FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("browserName", "firefox");
        options.setCapability("acceptInsecureCerts", true);
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);
        options.addPreference("startup.homepage_override_url", "about:blank");

        options.addArguments("--start-maximized",
                "--disable-web-security");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        return options;
    }
}

