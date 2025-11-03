package org.example.core.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Chrome extends DriverManager {
    @Override
    public void initDriver() {
        initDriverInternal();
    }

    @Override
    protected void initLocalDriver() {
        // WebDriverManager setup is handled centrally in DriverManager.setupDriverBinary() when use.wdm=true.
        ChromeOptions options = buildChromeOptions();
        driver = new ChromeDriver(options);

        // Log actual browser version after driver initialization
        try {
            if (driver instanceof HasCapabilities) {
                String actualVersion = ((HasCapabilities) driver).getCapabilities().getBrowserVersion();
                if (actualVersion != null) {
                    log.info("✅ Chrome driver initialized with browser version: {}", sanitizeVersion(actualVersion));
                }
            }
        } catch (Exception e) {
            log.debug("Could not get browser version from driver: {}", e.getMessage());
        }
    }

    @Override
    protected WebDriver createRemoteDriver(URL url, String browserVersion) {
        ChromeOptions options = buildRemoteChromeOptions(browserVersion);
        return new RemoteWebDriver(url, options);
    }

    private ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        options.addArguments(
                "--start-maximized",
                "--remote-allow-origins=*",
                "--disable-web-security",
                "--disable-features=VizDisplayCompositor"
        );

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        return options;
    }

    private ChromeOptions buildRemoteChromeOptions(String browserVersion) {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("browserName", "chrome");
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
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)+)");
        Matcher m = p.matcher(version);
        if (m.find()) {
            return m.group(1);
        }
        return version.trim();
    }
}

