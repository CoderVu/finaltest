package config;

import com.codeborne.selenide.WebDriverRunner;
import org.example.config.BrowserConfig;
import org.example.report.SoftAssertConfig;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

public class TestBase {

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            BrowserConfig.setUp();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        if (BrowserConfig.shouldSkipBrowser()) {
            throw new SkipException("Skipping test - browser not selected in single browser mode");
        }
        SoftAssertConfig.reset();
      
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
       
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        BrowserConfig.tearDown();
    }
}
