package org.example.pages;

import org.example.core.element.BaseElement;
import org.openqa.selenium.By;
import java.time.Duration;
import static org.example.utils.DriverUtils.navigateTo;

public class DynamicLoadPage extends BasePage {

    // Links to examples
    private final BaseElement exampleLink1 =
            BaseElement.$("//a[@href='/dynamic_loading/1']");

    private final BaseElement exampleLink2 =
            BaseElement.$("//a[@href='/dynamic_loading/2']");

    // Elements on the page
    private final BaseElement startButton =
            BaseElement.$(By.cssSelector("#start button"));

    private final BaseElement loadingDiv =
            BaseElement.$(By.cssSelector("#loading"));

    public final BaseElement finishText =
            BaseElement.$(By.cssSelector("#finish h4"));

    public void navigateToPage() {
        step("Navigate to Dynamic Loading page", () -> {
            navigateTo("https://the-internet.herokuapp.com/dynamic_loading");
        });
    }

    public void clickExample1() {
        step("Click Example 1 link", () -> {
            exampleLink1.waitForVisibility(Duration.ofSeconds(10));
            exampleLink1.click();
        });
    }

    public void clickExample2() {
        step("Click Example 2 link", () -> {
            exampleLink2.waitForVisibility(Duration.ofSeconds(10));
            exampleLink2.click();
        });
    }

    public void clickStartButton() {
        step("Click Start button", () -> {
            startButton.click();
        });
    }

    public void waitForLoadingToFinish() {
        step("Wait for loading to finish", () -> {
            // Try to wait for loading div to appear (may not appear in Example 1)
            try {
                loadingDiv.waitForVisibility(Duration.ofSeconds(1));
                logInfo("Loading started");
                
                // Wait for loading div to disappear (max 10 seconds)
                loadingDiv.waitForInvisibility(Duration.ofSeconds(10));
                logInfo("Loading finished");
            } catch (Exception e) {
                // Loading div may not appear (e.g., Example 1 where element is already in DOM)
                logInfo("Loading div not found or already finished, continuing...");
            }
        });
    }

    public boolean checkHelloWorldDisplay(String expectedText) {
        return step("Check if 'Hello World!' text is displayed", () -> {
            waitForLoadingToFinish();
            // getText() already has wait built-in, no need to wait separately
            String actualText = finishText.getText();
            logInfo("Finish text displayed: " + actualText);
            return actualText.equals(expectedText);
        });
    }
    public String getFinishText() {
        return step("Get finish text", () -> {
            String actualText = finishText.getText();
            logInfo("Finish text displayed: " + actualText);
            return actualText;
        });
    }
}
