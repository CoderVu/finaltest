package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.core.control.util.DriverUtils.sanitizeVersion;

public class Firefox extends AbstractDriverManager {

    public Firefox() {
        super(BrowserType.FIREFOX);
    }

    @Override
    public void initLocalDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);

        options.addArguments("--start-maximized",
                "--disable-web-security");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        driver = new FirefoxDriver(options);
    }

    @Override
    public WebDriver createRemoteDriver(URL url, String version) {
        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("browserName", "firefox");
        options.setCapability("acceptInsecureCerts", true);

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        if (version != null) {
            options.setBrowserVersion(sanitizeVersion(version));
        }

        driver = new org.openqa.selenium.remote.RemoteWebDriver(url, options);
        return driver;
    }
}
