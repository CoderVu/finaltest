package config;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.enums.BrowserType;
import org.example.core.assertion.Assert;
import org.example.core.driver.factory.DriverFactory;
import org.example.core.reporting.ReportManager;
import org.testng.ITestResult;
import org.testng.annotations.*;

import static org.example.utils.DriverUtils.getWebDriver;


@Slf4j
public class TestBase {

    static {
        ReportManager.getReporter();
    }

    private BrowserType browserType;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {

    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
    }

    @Parameters({"browser"})
    @BeforeClass(alwaysRun = true)
    public void setUpClass(@Optional String browser) {
        browserType = Config.getBrowserType(browser);
        log.debug("Setting up browser {}", browser);
        DriverFactory.createDriver(browserType);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        try {
            getWebDriver();
        } catch (Exception e) {
            DriverFactory.createDriver(browserType);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quitDriver();
    }
}
