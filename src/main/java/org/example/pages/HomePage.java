package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.constants.Constants;
import org.example.enums.Category;
import org.openqa.selenium.By;

import static org.example.utils.WebDriverUtils.getCurrentUrl;

public class HomePage extends BasePage{

    @Step("Navigate to home page")
    public void navigateToHomePage() {
       String current = getCurrentUrl();
       String base = Constants.getBaseUrl();
       if (current == null || !current.startsWith(base)) {
           Selenide.open(base);
       }
    }
    @Step("Back to home page")
    public void backToHome() {
        backToHomePage();
    }

    @Step("Check home page displayed")
    public boolean isHomePageDisplayed() {
        return getCurrentUrl().contains(Constants.getBaseUrl());
    }
    @Step("Select category: {categoryName}")
    public void selectCategory(Category categoryName) {
        // Try by title first, fallback to href; scroll into view before click
        By[] locators = new By[] { categoryName.byTitle(), categoryName.byHref() };
        SelenideElement el = null;
        for (By by : locators) {
            try {
                el = Selenide.$(by).shouldBe(Condition.visible);
                break;
            } catch (Throwable ignored) {
                // try next locator
            }
        }
        if (el == null) {
            // force last attempt by href to raise a clear error
            el = Selenide.$(categoryName.byHref()).shouldBe(Condition.visible);
        }
        el.scrollIntoView(true).click();
      }
}
