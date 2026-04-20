package testCase;

import config.TestBase;
import lombok.extern.slf4j.Slf4j;
import org.example.core.element.SelElement;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.example.core.assertion.AwaitAssert;

import static org.example.core.element.SelElement.$;
import static org.example.utils.DriverUtils.navigateTo;

@Slf4j
public class DynamicControlTest extends TestBase {

    private final SelElement checkbox = $(By.xpath("//input[@type='checkbox']"));
    private final SelElement removeAddButton = $(By.xpath("//button[@onclick='swapCheckbox()']"));
    private final SelElement message = $(By.xpath("//*[@id='message']"));
    private final SelElement enableDisableButton = $(By.xpath("//button[@onclick='swapInput()']"));
    private final SelElement textInput = $(By.xpath("//input[@type='text']"));

    @Test(description = "Validate Dynamic Controls with wait & retry")
    public void testDynamicControlsFlow() {

        navigateTo("https://the-internet.herokuapp.com/dynamic_controls");

        AwaitAssert.expect(checkbox).withTimeout(5000).toBeVisible();

        removeAddButton.click();
        AwaitAssert.expect(message).toHaveText("It's gonne!");
        AwaitAssert.expect(checkbox).toBeHidden();
        

        removeAddButton.click();
        AwaitAssert.expect(message).toHaveText("It's backk!");
        AwaitAssert.expect(checkbox).toBeVisible();

        // ---------- Enable Input ----------

        AwaitAssert.expect(textInput).toBeDisabled();

        enableDisableButton.click();
        AwaitAssert.expect(message).toHaveText("It's enabled!");
        textInput.setText("Hello World!");

        enableDisableButton.click();
        AwaitAssert.expect(message).toHaveText("It's disabled!");
        AwaitAssert.expect(textInput).toBeDisabled();
    }

}
