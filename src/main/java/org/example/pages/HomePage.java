package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.constants.Constants;
import org.example.enums.Category;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.*;

import static org.example.utils.WebDriverUtils.getCurrentUrl;

public class HomePage extends BasePage {

    // Locators
    protected final SelenideElement searchField = $("input[data-view-id='main_search_form_input'][placeholder='Freeship đơn từ 45k']");
    protected final SelenideElement categoryContainer = $x("//div[@data-view-id='search_top.category_product_container']");
    // Dynamic locators
    protected static final String dynamicCategoryByTitle = ".//a[@data-view-id='search_top.category_product_item' and .//span[@class='title' and normalize-space(text())='%s']]";
    protected static final String dynamicCategoryByHref = ".//a[@data-view-id='search_top.category_product_item' and contains(@href,'%s')]";

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

    @Step("Click on search field")
    public void clickSearchField() {
        searchField.shouldBe(Condition.visible).click();
    }


    @Step("Enter search text: {text}")
    public void setSearchText(String text) {
        searchField.shouldBe(Condition.visible).setValue(text);
    }

    @Step("Select category: {categoryName}")
    public void selectCategory(Category categoryName) {
        By[] locators = new By[]{categoryName.byTitle(), categoryName.byHref()};
        SelenideElement el = null;
        for (By by : locators) {
            try {
                el = Selenide.$(by).shouldBe(Condition.visible);
                break;
            } catch (Throwable ignored) {

            }
        }
        if (el == null) {
            el = Selenide.$(categoryName.byHref()).shouldBe(Condition.visible);
        }
        el.scrollIntoView(true).click();
    }

    @Step("Select search-top category: {categoryTitle}")
    public void selectSearchTopCategory(String categoryTitle) {
        if (!categoryContainer.is(Condition.visible)) {
            clickSearchField();
            if (!categoryContainer.is(Condition.visible)) {
                return; // container still not visible, skip
            }
        }
        SelenideElement container = categoryContainer;
        String itemXp = String.format(dynamicCategoryByTitle, categoryTitle);
        container.$x(itemXp).shouldBe(Condition.visible).click();
    }

    @Step("Select search-top category by enum: {category}")
    public void selectSearchTopCategory(Category category) {
        if (!categoryContainer.is(Condition.visible)) {
            clickSearchField();
            if (!categoryContainer.is(Condition.visible)) {
                return; // container still not visible, skip
            }
        }
        SelenideElement container = categoryContainer;
        String byTitleXp = String.format(dynamicCategoryByTitle, category.getTitle());
        String byHrefXp = String.format(dynamicCategoryByHref, category.getHref());
        SelenideElement target;
        try {
            target = container.$x(byTitleXp).shouldBe(Condition.visible);
        } catch (Throwable ignored) {
            target = container.$x(byHrefXp).shouldBe(Condition.visible);
        }
        target.click();
    }
}

