package config;

import org.example.config.BrowserConfig;
import org.example.config.SoftAssertConfig;
import org.example.pages.BasePage;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class TestBase {

    @BeforeMethod
    public void setUp() {
        BrowserConfig.setUp();
        new BasePage().closePopupIfPresent();
        SoftAssertConfig.reset();
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        BrowserConfig.tearDown();
    }
}