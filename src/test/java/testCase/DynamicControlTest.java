package testCase;

import config.TestBase;
import lombok.extern.slf4j.Slf4j;
import org.example.core.element.BaseElement;
import org.example.utils.DriverUtils;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static org.example.core.element.BaseElement.$;
import static org.example.utils.DriverUtils.navigateTo;

@Slf4j
public class DynamicControlTest extends TestBase {

    private final BaseElement checkbox = $(By.xpath("//input[@type='checkbox']"));
    private final BaseElement removeAddButton = $(By.xpath("//button[@onclick='swapCheckbox()']"));
    private final BaseElement message = $(By.xpath("//*[@id='message']"));
    private final BaseElement enableDisableButton = $(By.xpath("//button[@onclick='swapInput()']"));
    private final BaseElement textInput = $(By.xpath("//input[@type='text']"));

    @Test(description = "Validate Dynamic Controls with wait & retry")
    public void testDynamicControlsFlow() {

        navigateTo("https://the-internet.herokuapp.com/dynamic_controls");

        checkbox.shouldBeVisible();

        removeAddButton.click();
        message.shouldHaveText("It's gone!");
        checkbox.shouldNotBeVisible();

        removeAddButton.click();
        message.shouldHaveText("It's back!");
        checkbox.shouldBeVisible().click();

        // ---------- Enable Input ----------

        textInput.shouldBeDisabled();

        enableDisableButton.click();
        message.shouldHaveText("It's enabled!");
        textInput.setText("Hello World!");

        enableDisableButton.click();
        message.shouldHaveText("It's disabled!");
        textInput.shouldBeDisabled();
    }

}
