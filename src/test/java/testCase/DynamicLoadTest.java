package testCase;

import org.example.pages.DynamicLoadPage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import static org.example.core.assertion.AssertionRetry.assertEquals;
import static org.example.core.assertion.AssertionRetry.assertTrue;

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

        // Check Hello World text is correct
        assertTrue(
            () -> dynamicLoadPage.checkHelloWorldDisplay("Hello World!"),
            "Hello World text should be correct"
        );
    }

//    @Test(description = "Test Example 2: Element rendered after the fact")
//    public void testExample2() {
//        // Navigate to dynamic loading page
//        dynamicLoadPage.navigateToPage();
//
//        // Click Example 2 link
//        dynamicLoadPage.clickExample2();
//
//        // Click Start button
//        dynamicLoadPage.clickStartButton();
//
//        // Wait for loading to finish
//        dynamicLoadPage.waitForLoadingToFinish();
//
//        // Check Hello World text is displayed
//        assertEquals(
//            () -> dynamicLoadPage.checkHelloWorldDisplay(),
//            true,
//            "Hello World text should be displayed"
//        );
//    }
}

