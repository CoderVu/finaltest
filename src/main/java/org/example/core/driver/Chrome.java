package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.core.control.util.DriverUtils.sanitizeVersion;

public class Chrome extends AbstractDriverManager {

    public Chrome() {
        super(BrowserType.CHROME);
    }

    @Override
    public void initLocalDriver() {
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

        driver = new ChromeDriver(options);
    }

    @Override
    public WebDriver createRemoteDriver(URL url, String version) {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("browserName", "chrome");
        options.setCapability("acceptInsecureCerts", true);

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        if (version != null) {
            options.setBrowserVersion(sanitizeVersion(version));
        }

        driver = new RemoteWebDriver(url, options);
        return driver;
    }
}
