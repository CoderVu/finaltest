package org.example.core.browser.edge;

import com.codeborne.selenide.Configuration;

import java.util.Arrays;
import java.util.Map;

public class EdgeBrowserConfig {
    public static void configure() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("ms:edgeOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--disable-web-security",
                                "--disable-features=VizDisplayCompositor",
                                "--disable-extensions",
                                "--disable-plugins",
                                "--disable-images",
                                "--disable-javascript",
                                "--no-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu",
                                "--remote-debugging-port=9222"
                        ),
                        "excludeSwitches", Arrays.asList("enable-automation", "enable-logging"),
                        "useAutomationExtension", false,
                        "detach", true
                )
        );
        Configuration.browserCapabilities.setCapability("platformName", "windows");
        Configuration.browserCapabilities.setCapability("ms:edgeChromium", true);
    }
}


