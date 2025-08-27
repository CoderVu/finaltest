package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class BasePage {

    //locator
    private final SelenideElement closePopupButton = $(By.cssSelector("picture.webpimg-container img[alt='close-icon']"));

    //method
    public void closePopupIfPresent() {
        try {
            closePopupButton.shouldBe(Condition.visible, Duration.ofSeconds(10));
            closePopupButton.click();
            log.info("Closed popup");
        } catch (Exception e) {
            log.info("No popup appeared within 10 seconds, continuing...");
        }
    }
}
