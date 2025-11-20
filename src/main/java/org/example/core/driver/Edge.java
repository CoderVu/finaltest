package org.example.core.driver;

import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class Edge extends AbstractDriverManager {

    public Edge() {
        super(BrowserType.EDGE);
    }

    @Override
    public void initLocalDriver() {
        driver = new EdgeDriver(buildEdgeOptions(false));
    }

    @Override
    protected EdgeOptions createRemoteOptions() {
        return buildEdgeOptions(true);
    }

    private EdgeOptions buildEdgeOptions(boolean forRemote) {
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
        if (forRemote) {
            options.setCapability("browserName", "MicrosoftEdge");
        }
        return options;
    }
}
