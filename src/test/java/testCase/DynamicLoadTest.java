package testCase;

import org.example.core.assertion.retry.AwaitAssert;
import org.example.core.element.BaseElement;
import org.example.pages.DynamicLoadPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;
import org.example.utils.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

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
        AwaitAssert.assertEquals(() ->
            classNew, "Hello World!",
            "Finish text should be 'Hello World!'"
        );

    }

    @Test(description = "Set filtered results to 'Retest' in TestRail using framework utilities")
    public void testSetResultsToReTest() {
        // Credentials: prefer system properties then environment variables
        String username = System.getProperty("testrail.user", System.getenv("TESTRAIL_USER"));
        String password = System.getProperty("testrail.pass", System.getenv("TESTRAIL_PASS"));
        if (username == null || password == null) {
            log.error("TestRail credentials not provided. Set -Dtestrail.user / -Dtestrail.pass or env TESTRAIL_USER / TESTRAIL_PASS");
            return;
        }

        final String baseUrl = "https://sonos.testrail.com";
        final String runUrl = baseUrl + "/index.php?/runs/view/144115";

        // navigate to TestRail and login
        DriverUtils.getWebDriver().get(baseUrl);
        BaseElement.$(By.id("name")).setText(username);
        BaseElement.$(By.id("password")).setText(password);
        BaseElement.$(By.id("button_primary")).click();

        // navigate directly to run page (keeps filter state)
        DriverUtils.getWebDriver().get(runUrl);

        // loop through rows and set each to Retest (value = "4")
        while (true) {
            // refetch rows each pass
            List<WebElement> testRows = BaseElement.$(By.xpath("//td[@data-testid='runTestAction']")).getElements();
            if (testRows == null || testRows.isEmpty()) {
                log.info("No more test rows to process.");
                break;
            }

            boolean progressed = false;
            for (WebElement row : testRows) {
                try {
                    // wait for overlays to clear
                    WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-widget-overlay, .blockUI")), Duration.ofSeconds(5));

                    // find expand/toggle button inside the row
                    WebElement expandBtn;
                    try {
                        expandBtn = row.findElement(By.xpath(".//a[contains(@onclick,'App.QPane.toggleRow')]"));
                    } catch (Exception e) {
                        expandBtn = row.findElement(By.xpath(".//a[.//div[@data-testid='runTestIconExpandRow']]"));
                    }

                    // open row safely
                    try {
                        expandBtn.click();
                    } catch (Exception e) {
                        // fallback to JS click
                        BaseElement.$(By.xpath("//*")).clickByJs(); // noop usage to ensure JS executor available
                        DriverUtils.execJavaScript("arguments[0].click();", expandBtn);
                    }

                    // attempt to find the edit link scoped to the row, otherwise fallback to global one
                    WebElement editLink = null;
                    try {
                        editLink = row.findElement(By.xpath(".//span[@class='text-secondary editChange']/a"));
                    } catch (Exception ex) {
                        try {
                            editLink = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='text-secondary editChange']/a")));
                        } catch (Exception ignored) {
                            editLink = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='text-secondary editChange']/a")));
                        }
                    }

                    // scroll and click edit
                    try {
                        DriverUtils.execJavaScript("arguments[0].scrollIntoView({block:'center'});", editLink);
                        editLink.click();
                    } catch (Exception e) {
                        try {
                            DriverUtils.execJavaScript("arguments[0].click();", editLink);
                        } catch (Exception ee) {
                            log.warn("Failed to click edit link: {}", ee.getMessage());
                            // collapse and continue
                            try { expandBtn.click(); } catch (Exception ignore) {}
                            continue;
                        }
                    }

                    // wait for status select to appear
                    WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(By.id("addResultStatus")));

                    // set status to Retest (value "4")
                    try {
                        WebElement statusElem = DriverUtils.getWebDriver().findElement(By.id("addResultStatus"));
                        try {
                            new Select(statusElem).selectByValue("4");
                        } catch (Exception selEx) {
                            // fallback for custom select: set value via JS and trigger change
                            DriverUtils.execJavaScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'));", statusElem, "4");
                        }
                    } catch (Exception e) {
                        log.warn("Status select not available or interactable: {}", e.getMessage());
                    }

                    // click submit
                    try {
                        WebElement submitBtn = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.id("addResultSubmit")));
                        submitBtn.click();
                        // wait for submit dialog/overlay to disappear
                        WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.id("addResultStatus")), Duration.ofSeconds(10));
                        WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.className("blockUI")), Duration.ofSeconds(10));
                        log.info("Set to 'Retest' and submitted for a row.");
                    } catch (Exception e) {
                        log.warn("Failed to submit result: {}", e.getMessage());
                        // try to collapse and continue
                        try { expandBtn.click(); } catch (Exception ignore) {}
                        continue;
                    }

                    // Close the QPane edit panel so we can move to another row
                    try {
                        WebElement closeBtn = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.xpath("//div[@data-testid='qpaneCloseButton']")), Duration.ofSeconds(5));
                        try { DriverUtils.execJavaScript("arguments[0].scrollIntoView({block:'center'});", closeBtn); } catch (Exception ignored) {}
                        try { closeBtn.click(); } catch (Exception ex) { DriverUtils.execJavaScript("arguments[0].click();", closeBtn); }
                        WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@data-testid='qpaneCloseButton']")), Duration.ofSeconds(5));
                    } catch (Exception e) {
                        log.warn("Could not click qpane close button, proceeding: {}", e.getMessage());
                    }

                    // collapse the row to avoid re-processing
                    try {
                        expandBtn.click();
                    } catch (Exception e) {
                        try { DriverUtils.execJavaScript("arguments[0].click();", expandBtn); } catch (Exception ignore) {}
                    }

                    progressed = true;
                    // after editing one, break to refetch rows
                    break;
                } catch (Exception e) {
                    log.warn("Error processing row: {}", e.getMessage());
                    // continue with next row
                }
            }

            if (!progressed) {
                log.info("No progress in pass - exiting loop.");
                break;
            }
        }

        log.info("Finished setting filtered results to Retest.");
    }
}

