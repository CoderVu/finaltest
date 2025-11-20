package org.example.core.driver;

import org.example.configure.Config;
import org.example.core.driver.manager.RemoteDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Firefox extends RemoteDriverManager {

    public Firefox() {
        super(BrowserType.FIREFOX);
    }

    @Override
    protected WebDriver createLocalDriver() {
        return new FirefoxDriver(buildFirefoxOptions(false));
    }

    @Override
    protected FirefoxOptions createRemoteOptions() {
        return buildFirefoxOptions(true);
    }

    private FirefoxOptions buildFirefoxOptions(boolean forRemote) {
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

        if (forRemote) {
            options.setCapability("browserName", "firefox");
        }
        return options;
    }
}
