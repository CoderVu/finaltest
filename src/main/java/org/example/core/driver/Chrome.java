package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;

public class Chrome extends AbstractDriverManager {

    public Chrome() {
        super(BrowserType.CHROME);
    }

    @Override
    public void initLocalDriver() {
        driver = new ChromeDriver(buildChromeOptions(false));
    }

    @Override
    protected ChromeOptions createRemoteOptions() {
        return buildChromeOptions(true);
    }

    private ChromeOptions buildChromeOptions(boolean forRemote) {
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

        if (forRemote) {
            options.setCapability("browserName", "chrome");
        }
        return options;
    }
}
