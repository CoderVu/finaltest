package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.example.constants.Constants;
import org.example.enums.Category;

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
    @Step("Select category: {categoryName}")
    public void selectCategory(Category categoryName) {
        Selenide.$(categoryName.byTitle()).shouldBe(Condition.visible).click();
      }
}
