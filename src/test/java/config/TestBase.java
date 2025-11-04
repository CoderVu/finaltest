package config;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.DriverFactory;
import org.example.core.report.ConsoleConfig;
import org.example.core.report.SoftAssertConfig;
import org.example.enums.BrowserType;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

@Slf4j
public class TestBase {

    private static final ConsoleConfig logConfig = new ConsoleConfig();
    private BrowserType browserType;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logConfig.startTerminalLog();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logConfig.stopTerminalLog();
    }

    @Parameters({"browser"})
    @BeforeClass(alwaysRun = true)
    public void setUpClass(@Optional String browser) {

        if (browser != null && !browser.trim().isEmpty()) {
            log.info("=== TestNG parameter browser provided: {} ===", browser);
            try {
                browserType = BrowserType.valueOf(browser.trim().toUpperCase());
            } catch (Exception e) {
                log.warn("Unknown browser parameter '{}', falling back to Config.getBrowserType()", browser);
                browserType = Config.getBrowserType();
            }
        } else {
            browserType = Config.getBrowserType();
            log.info("=== Using default browser from Config: {} ===", browserType);
        }

        Config.setThreadBrowser(browserType.toString().toLowerCase());

        if (Config.shouldSkipBrowser()) {
            throw new SkipException("Skipping test - browser not selected in single browser mode");
        }

        DriverFactory.getDriverManager(browserType);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        if (Config.shouldSkipBrowser()) {
            throw new SkipException("Skipping test - browser not selected in single browser mode");
        }
        SoftAssertConfig.reset();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
     // implement after
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quitDriver();
        Config.setThreadBrowser(null);
    }
}
