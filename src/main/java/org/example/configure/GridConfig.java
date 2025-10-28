package org.example.configure;

import com.codeborne.selenide.WebDriverProvider;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class GridConfig implements WebDriverProvider {

    @Override
    @Nonnull
    public WebDriver createDriver(@Nonnull Capabilities ignored) {
        String browser = ExecutionConfig.getBrowser();
        String requestedVersion = resolveBrowserVersion();

        if (!ExecutionConfig.isRemoteEnabled()) {
            return createLocalDriver(browser);
        }

        return createRemoteDriver(browser, requestedVersion);
    }

    // ---------------- LOCAL DRIVER ----------------
    private WebDriver createLocalDriver(String browser) {
        switch (browser) {
            case "firefox":
                FirefoxOptions ff = new FirefoxOptions();
                if (ExecutionConfig.isHeadless()) {
                    ff.addArguments("--headless=new", "--disable-gpu");
                }
                // Prefer Selenium Manager. Optionally allow WDM via -Duse.wdm=true
                if (Boolean.getBoolean("use.wdm")) {
                    try {
                        WebDriverManager.firefoxdriver().setup();
                        log.info("Using WebDriverManager for Firefox driver setup");
                    } catch (Exception ex) {
                        log.warn("WDM failed for geckodriver, using Selenium Manager: {}", ex.getMessage());
                    }
                } else {
                    log.info("Using Selenium Manager for Firefox driver resolution");
                }
                // Basic Windows binary check to avoid 'binary not found'
                try {
                    String[] candidates = new String[] {
                            "C\\\\Program Files\\\\Mozilla Firefox\\\\firefox.exe",
                            "C\\\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe"
                    };
                    java.io.File found = null;
                    for (String c : candidates) { java.io.File f = new java.io.File(c); if (f.exists()) { found = f; break; } }
                    if (found != null) { ff.setBinary(found.getAbsolutePath()); }
                } catch (Throwable ignored) {}
                return new org.openqa.selenium.firefox.FirefoxDriver(ff);

            case "edge":
                EdgeOptions edge = new EdgeOptions();
                if (ExecutionConfig.isHeadless()) {
                    edge.addArguments("--headless=new", "--disable-gpu");
                }
                if (Boolean.getBoolean("use.wdm")) {
                    try {
                        WebDriverManager.edgedriver().setup();
                        log.info("Using WebDriverManager for Edge driver setup");
                    } catch (Exception ex) {
                        log.warn("WDM failed for msedgedriver, using Selenium Manager: {}", ex.getMessage());
                    }
                } else {
                    log.info("Using Selenium Manager for Edge driver resolution (avoids version mismatch)");
                }
                return new org.openqa.selenium.edge.EdgeDriver(edge);

            case "chrome":
            default:
                ChromeOptions ch = new ChromeOptions();
                if (ExecutionConfig.isHeadless()) {
                    ch.addArguments("--headless=new", "--disable-gpu");
                }
                if (Boolean.getBoolean("use.wdm")) {
                    try {
                        WebDriverManager.chromedriver().setup();
                        log.info("Using WebDriverManager for Chrome driver setup");
                    } catch (Exception ex) {
                        log.warn("WDM failed for chromedriver, using Selenium Manager: {}", ex.getMessage());
                    }
                } else {
                    log.info("Using Selenium Manager for Chrome driver resolution");
                }
                return new org.openqa.selenium.chrome.ChromeDriver(ch);
        }
    }

    // ---------------- REMOTE DRIVER ----------------
    private WebDriver createRemoteDriver(String browser, String requestedVersion) {
        String remoteUrl = ExecutionConfig.getRemoteUrl();
        URL url;
        try {
            url = new URL(remoteUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid remote.url: " + remoteUrl, e);
        }

        switch (browser) {
            case "firefox":
                FirefoxOptions ff = new FirefoxOptions();
                ff.setCapability("browserName", "firefox");
                if (requestedVersion != null) ff.setCapability("browserVersion", requestedVersion);
                ff.setCapability("platformName", "linux");
                if (ExecutionConfig.isHeadless()) {
                    ff.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, ff);

            case "edge":
                EdgeOptions edge = new EdgeOptions();
                edge.setCapability("browserName", "MicrosoftEdge");
                if (requestedVersion != null) edge.setCapability("browserVersion", requestedVersion);
                edge.setCapability("platformName", "linux");
                if (ExecutionConfig.isHeadless()) {
                    edge.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, edge);

            case "chrome":
            default:
                ChromeOptions ch = new ChromeOptions();
                ch.setCapability("browserName", "chrome");
                if (requestedVersion != null) ch.setCapability("browserVersion", requestedVersion);
                ch.setCapability("platformName", "linux");
                if (ExecutionConfig.isHeadless()) {
                    ch.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, ch);
        }
    }

    // ---------------- RESOLVE BROWSER ----------------
    

    // ---------------- RESOLVE VERSION ----------------
    private String resolveBrowserVersion() {
        try {
            if (Reporter.getCurrentTestResult() != null
                    && Reporter.getCurrentTestResult().getTestContext() != null
                    && Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest() != null) {
                String param = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("browserVersion");
                if (param != null && !param.trim().isEmpty()) {
                    log.info("GridConfig: Using TestNG parameter browserVersion: {}", param);
                    return param.trim();
                }
            }
        } catch (Throwable ignored) {}

        String sys = System.getProperty("browser.version");
        if (sys != null && !sys.trim().isEmpty()) {
            log.info("GridConfig: Using system property browser.version: {}", sys);
            return sys.trim();
        }

        return null; 
    }
}
