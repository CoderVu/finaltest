package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.constants.Constants;
import org.example.enums.Category;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static org.example.utils.WebDriverUtils.getCurrentUrl;

public class HomePage {

    @Step("Navigate to home page")
    public void navigateToHomePage() {
        String current = getCurrentUrl();
        String base = Constants.getBaseUrl();
        if (current == null || !current.startsWith(base)) {
            Selenide.open(base);
        }
    }

    @Step("Check home page displayed")
    public boolean isHomePageDisplayed() {
        return getCurrentUrl().contains(Constants.getBaseUrl());
    }
    
    @Step("Close popup if present")
    private void closePopupIfPresent() {
        try {
            SelenideElement popup = $(".modal-overlay, .popup-overlay, .modal");
            if (popup.exists()) {
                SelenideElement closeButton = popup.$(".close, .btn-close, [aria-label='Close']");
                if (closeButton.exists()) {
                    closeButton.click();
                }
            }
        } catch (Exception e) {
            // Popup might not exist, continue
        }
    }
    
    @Step("Navigate to category: {categoryName}")
    public BookCategoryPage selectCategory(Category categoryName) {
        closePopupIfPresent();
        SelenideElement categoryElement = $(categoryName.byTitle());
        categoryElement.shouldBe(Condition.visible, Duration.ofSeconds(10));
        categoryElement.click();
        return new BookCategoryPage();
    }
}
