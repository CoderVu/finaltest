package org.example.core.browser.chrome;

import com.codeborne.selenide.Configuration;

import java.util.Arrays;
import java.util.Map;

public class ChromeBrowserConfig {
    public static void configure() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("goog:chromeOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--remote-allow-origins=*",
                                "--disable-web-security",
                                "--disable-features=VizDisplayCompositor"
                        ),
                        "excludeSwitches", Arrays.asList("enable-automation"),
                        "useAutomationExtension", false
                )
        );
    }
}


