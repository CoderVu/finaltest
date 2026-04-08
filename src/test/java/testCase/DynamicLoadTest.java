package testCase;

import org.example.pages.DynamicLoadPage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicLoadTest extends TestBase {

    DynamicLoadPage dynamicLoadPage = new DynamicLoadPage();

    @Test(description = "Test Example 1: Element on page that is hidden")
    public void testExample1() {
        // Navigate to dynamic loading page
        dynamicLoadPage.navigateToPage();

        // Click Example 1 link
        dynamicLoadPage.clickExample1();

        // Click Start button
        dynamicLoadPage.clickStartButton();

        dynamicLoadPage.shouldDisplayHelloWorld("Hello WorldL!");
    }
}

