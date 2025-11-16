package config;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.DriverFactory;
import org.example.core.assertion.SoftAssertImpl;
import org.example.core.report.ReportManager;
import org.example.enums.BrowserType;
import org.testng.ITestResult;
import org.testng.annotations.*;

import static org.example.common.Constants.loadEnvironment;


@Slf4j
public class TestBase {

    static {
        // Initialize report system (Allure config is handled by pom.xml)
        ReportManager.initReport();
    }

    private BrowserType browserType;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        loadEnvironment(Config.getEnvFile());
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
        SoftAssertImpl.reset();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
     // implement after
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quitDriver();
    }
}
