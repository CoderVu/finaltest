package org.example.pages;

import com.codeborne.selenide.Condition;
import lombok.extern.slf4j.Slf4j;
import org.example.core.control.common.annotation.FindBy;
import org.example.core.control.common.imp.Element;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class BasePage {


    public BasePage() {
        PageFactory.initElements(this);
    }
    //controls
    @FindBy(css = "picture.webpimg-container img[alt='close-icon']")
    protected Element closePopupButton;

    @FindBy(css = "a.tiki-logo[data-view-id='header_main_logo']")
    protected Element logo;

    //method
    public void closePopupIfPresent() {
        try {
            $(By.cssSelector("picture.webpimg-container img[alt='close-icon']")).shouldBe(Condition.visible, Duration.ofSeconds(10));
            closePopupButton.click();
            log.info("Closed popup successfully.");
        } catch (Exception e) {
            log.info("No popup appeared within 10 seconds, continuing...");
        }
    }

    protected void backToHomePage() {
        $("a.tiki-logo[data-view-id='header_main_logo']").shouldBe(Condition.visible);
        logo.click();
        closePopupIfPresent();
    }
}