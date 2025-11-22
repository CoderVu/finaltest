package org.example.core.driver;

import org.example.configure.Config;
import org.example.core.driver.manager.AbstractDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;

public class Chrome extends AbstractDriverManager {

    public Chrome() {
        super(BrowserType.CHROME);
    }

    @Override
    protected WebDriver createLocalDriver() {
        return new ChromeDriver(buildChromeOptions());
    }

    @Override
    protected ChromeOptions createRemoteOptions() {
        return buildChromeOptions();
    }

    public static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("browserName", "chrome");
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
}

