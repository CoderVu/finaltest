package org.example.core.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Firefox extends DriverManager {
    @Override
    public void initDriver() {
        initDriverInternal();
    }

    @Override
    protected void initLocalDriver() {
        // WebDriverManager setup is handled centrally in DriverManager.setupDriverBinary() when use.wdm=true.
        FirefoxOptions options = buildFirefoxOptions();
        driver = new FirefoxDriver(options);

        try {
            if (driver instanceof HasCapabilities) {
                String actualVersion = ((HasCapabilities) driver).getCapabilities().getBrowserVersion();
                if (actualVersion != null) {
                    log.info("✅ Firefox driver initialized with browser version: {}", sanitizeVersion(actualVersion));
                }
            }
        } catch (Exception e) {
            log.debug("Could not get browser version from driver: {}", e.getMessage());
        }
    }

    @Override
    protected WebDriver createRemoteDriver(URL url, String browserVersion) {
        FirefoxOptions options = buildRemoteFirefoxOptions(browserVersion);
        return new RemoteWebDriver(url, options);
    }

    private FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.addArguments("--start-maximized", "--disable-web-security");
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        
        return options;
    }

    private FirefoxOptions buildRemoteFirefoxOptions(String browserVersion) {
        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("browserName", "firefox");
        if (browserVersion != null) {
            options.setCapability("browserVersion", browserVersion);
        }
        options.setCapability("platformName", resolvePlatformName());
        options.setCapability("acceptInsecureCerts", true);
        
        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }
        
        return options;
    }

    private String sanitizeVersion(String version) {
        if (version == null) {
            return null;
        }
        // Extract canonical numeric version like 144.0.2 or 141.0.7390.123 if present.
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)+)");
        Matcher m = p.matcher(version);
        if (m.find()) {
            return m.group(1);
        }
        return version.trim();
    }
}

