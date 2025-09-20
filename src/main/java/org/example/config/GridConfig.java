package org.example.config;

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
        String browser = resolveBrowser();
        String requestedVersion = resolveBrowserVersion();

        if (!BrowserConfig.isRemoteEnabled()) {
            return createLocalDriver(browser);
        }

        return createRemoteDriver(browser, requestedVersion);
    }

    // ---------------- LOCAL DRIVER ----------------
    private WebDriver createLocalDriver(String browser) {
        switch (browser) {
            case "firefox":
                FirefoxOptions ff = new FirefoxOptions();
                if (BrowserConfig.isHeadless()) {
                    ff.addArguments("--headless=new", "--disable-gpu");
                }
                try {
                    WebDriverManager.firefoxdriver().setup();
                } catch (Exception ex) {
                    log.warn("WDM failed for geckodriver, fallback to Selenium Manager: {}", ex.getMessage());
                }
                return new org.openqa.selenium.firefox.FirefoxDriver(ff);

            case "edge":
                EdgeOptions edge = new EdgeOptions();
                if (BrowserConfig.isHeadless()) {
                    edge.addArguments("--headless=new", "--disable-gpu");
                }
                try {
                    WebDriverManager.edgedriver().setup();
                } catch (Exception ex) {
                    log.warn("WDM failed for msedgedriver, fallback to Selenium Manager: {}", ex.getMessage());
                }
                return new org.openqa.selenium.edge.EdgeDriver(edge);

            case "chrome":
            default:
                ChromeOptions ch = new ChromeOptions();
                if (BrowserConfig.isHeadless()) {
                    ch.addArguments("--headless=new", "--disable-gpu");
                }
                try {
                    WebDriverManager.chromedriver().setup();
                } catch (Exception ex) {
                    log.warn("WDM failed for chromedriver, fallback to Selenium Manager: {}", ex.getMessage());
                }
                return new org.openqa.selenium.chrome.ChromeDriver(ch);
        }
    }

    // ---------------- REMOTE DRIVER ----------------
    private WebDriver createRemoteDriver(String browser, String requestedVersion) {
        String remoteUrl = BrowserConfig.getRemoteUrl();
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
                if (BrowserConfig.isHeadless()) {
                    ff.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, ff);

            case "edge":
                EdgeOptions edge = new EdgeOptions();
                edge.setCapability("browserName", "MicrosoftEdge");
                if (requestedVersion != null) edge.setCapability("browserVersion", requestedVersion);
                edge.setCapability("platformName", "linux");
                if (BrowserConfig.isHeadless()) {
                    edge.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, edge);

            case "chrome":
            default:
                ChromeOptions ch = new ChromeOptions();
                ch.setCapability("browserName", "chrome");
                if (requestedVersion != null) ch.setCapability("browserVersion", requestedVersion);
                ch.setCapability("platformName", "linux");
                if (BrowserConfig.isHeadless()) {
                    ch.addArguments("--headless=new", "--disable-gpu");
                }
                return new RemoteWebDriver(url, ch);
        }
    }

    // ---------------- RESOLVE BROWSER ----------------
    private String resolveBrowser() {
        try {
            if (Reporter.getCurrentTestResult() != null
                    && Reporter.getCurrentTestResult().getTestContext() != null
                    && Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest() != null) {
                String param = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("browser");
                if (param != null && !param.trim().isEmpty()) {
                    log.info("GridConfig: Using TestNG parameter browser: {}", param);
                    return param.trim().toLowerCase();
                }
            }
        } catch (Throwable ignored) {}

        String sys = System.getProperty("browser");
        if (sys != null && !sys.trim().isEmpty()) {
            log.info("GridConfig: Using system property browser: {}", sys);
            return sys.trim().toLowerCase();
        }

        String defaultBrowser = org.example.common.Constants.getDefaultBrowser();
        if (defaultBrowser != null && !defaultBrowser.trim().isEmpty()) {
            log.info("GridConfig: Using default browser: {}", defaultBrowser);
            return defaultBrowser.trim().toLowerCase();
        }

        log.info("GridConfig: Using fallback browser: chrome");
        return "chrome";
    }

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
