package org.example.core.browser.firefox;

import com.codeborne.selenide.Configuration;

import java.util.Arrays;
import java.util.Map;

public class FirefoxBrowserConfig {
    public static void configure() {
        Configuration.browserCapabilities.setCapability("acceptInsecureCerts", true);
        Configuration.browserCapabilities.setCapability("moz:firefoxOptions",
                Map.of(
                        "args", Arrays.asList(
                                "--start-maximized",
                                "--disable-web-security"
                        ),
                        "prefs", Map.of(
                                "dom.webdriver.enabled", false,
                                "useAutomationExtension", false
                        )
                )
        );
    }
}


