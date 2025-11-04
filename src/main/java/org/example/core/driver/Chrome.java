package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
