package org.example.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class BasePage {

    //locator
    private final SelenideElement closePopupButton = $(By.cssSelector("picture.webpimg-container img[alt='close-icon']"));

    //method
    public void closePopupIfPresent() {
        if (closePopupButton.exists() && closePopupButton.isDisplayed()) {
            closePopupButton.click();
            log.info("Closed popup");
        }
    }
}
