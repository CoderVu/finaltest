package org.example.core.driver;

import org.example.configure.Config;
import org.example.core.driver.manager.AbstractDriverManager;
import org.example.enums.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class Edge extends AbstractDriverManager {

    public Edge() {
        super(BrowserType.EDGE);
    }

    @Override
    protected WebDriver createLocalDriver() {
        return new EdgeDriver(buildEdgeOptions());
    }

    @Override
    protected EdgeOptions createRemoteOptions() {
        return buildEdgeOptions();
    }

    public static EdgeOptions buildEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability("browserName", "MicrosoftEdge");
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
        return options;
    }
}

