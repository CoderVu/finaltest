package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.net.URL;

import static org.example.core.element.util.DriverUtils.sanitizeVersion;

public class Edge extends AbstractDriverManager {

    public Edge() {
        super(BrowserType.EDGE);
    }

    @Override
    public void initLocalDriver() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.setCapability("ms:edgeChromium", true);

        options.addArguments(
                "--start-maximized",
                "--disable-web-security",
                "--disable-features=VizDisplayCompositor",
                "--disable-extensions",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        );

        if (Config.isHeadless()) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        driver = new EdgeDriver(options);
    }

    @Override
    public WebDriver createRemoteDriver(URL url, String version) {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("browserName", "MicrosoftEdge");
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
