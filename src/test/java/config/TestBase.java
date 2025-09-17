package config;

import org.example.config.BrowserConfig;
import org.example.report.SoftAssertConfig;
import org.example.pages.BasePage;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

public class TestBase {

    @Parameters("browser")
    @BeforeTest(alwaysRun = true)
    public void setBrowser(String browser, ITestContext context) {
        // Không set browser property để TestNG parameters có thể hoạt động
        // Browser sẽ được resolve từ TestNG parameter trong BrowserConfig.getBrowser()
        System.out.println("TestBase: TestNG parameter browser: " + browser);
        System.out.println("TestBase: Current system browser: " + System.getProperty("browser"));
    }

    @BeforeMethod(alwaysRun = true)
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