package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        options.addArguments("--start-maximized", "--disable-web-security");

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

    private String sanitizeVersion(String version) {
        if (version == null) return null;
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)+)");
        Matcher matcher = pattern.matcher(version);
        return matcher.find() ? matcher.group(1) : version.trim();
    }
}
