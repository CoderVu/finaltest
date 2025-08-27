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
    protected final SelenideElement closePopupButton = $(By.cssSelector("picture.webpimg-container img[alt='close-icon']"));
    protected final SelenideElement logo = $("a.tiki-logo[data-view-id='header_main_logo']");

    //method
    public void closePopupIfPresent() {
        try {
            closePopupButton.shouldBe(Condition.visible, Duration.ofSeconds(10));
            closePopupButton.click();
            log.info("Closed popup successfully.");
        } catch (Exception e) {
            log.info("No popup appeared within 10 seconds, continuing...");
        }
    }

    protected void backToHomePage() {
        logo.shouldBe(Condition.visible).click();
        closePopupIfPresent();
    }
    protected void setText(SelenideElement element, String value) {
        element.clear();
        element.shouldBe(Condition.visible).setValue(value != null ? value : "");
    }

    protected String getText(SelenideElement element) {
        return element.shouldBe(Condition.visible).getText();
    }
}