package testCase;

import org.example.core.assertion.retry.ElementAssertions;
import org.example.core.assertion.retry.FunctionAssertions;
import org.example.core.element.BaseElement;
import org.example.pages.DynamicLoadPage;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import static org.example.core.element.BaseElement.$;

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

        // way 1: Using FunctionAssertions with built-in retry assertions
        // FunctionAssertions.assertEquals(() ->
        //     dynamicLoadPage.getFinishText(), "Hello World!!",
        //     "Finish text should be 'Hello World!'"
        // );
        // way 2: Using BaseElement with built-in retry assertions
        BaseElement finishText = $(By.xpath("//*[@id='finish']/h4"));
        String classNew = finishText.shouldBeVisible().getText();
        FunctionAssertions.assertEquals(() ->
            classNew, "Hello World!",
            "Finish text should be 'Hello World!'"
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

