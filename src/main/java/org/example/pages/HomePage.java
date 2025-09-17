package org.example.pages;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.example.common.Constants;
import org.example.enums.Category;
import org.example.core.control.common.annotation.FindBy;
import org.example.core.control.common.imp.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.example.core.control.util.DriverUtils;

import static org.example.core.control.util.DriverUtils.getCurrentUrl;

public class HomePage extends BasePage {

    public HomePage() { super(); }

    @FindBy(css = "input[data-view-id='main_search_form_input'][placeholder='Freeship đơn từ 45k']")
    protected Element searchField;

    @FindBy(xpath = "//div[@data-view-id='search_top.category_product_container']")
    protected Element categoryContainer;
    
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
        searchField.click();
    }


    @Step("Enter search text: {text}")
    public void setSearchText(String text) {
        searchField.waitForDisplay();
        searchField.setValue(text);
    }

    @Step("Select category: {categoryName}")
    public void selectCategory(Category categoryName) {
        By[] locators = new By[]{categoryName.byTitle(), categoryName.byHref()};
        WebElement target = null;
        for (By by : locators) {
            try {
                WebDriverWait wait = new WebDriverWait(DriverUtils.getWebDriver(), Duration.ofSeconds(DriverUtils.getTimeOut()));
                target = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                if (target != null) break;
            } catch (Throwable ignored) {
            }
        }
        if (target == null) {
            try {
                WebDriverWait wait = new WebDriverWait(DriverUtils.getWebDriver(), Duration.ofSeconds(DriverUtils.getTimeOut()));
                target = wait.until(ExpectedConditions.presenceOfElementLocated(categoryName.byHref()));
            } catch (Throwable ignored) {
            }
        }
        if (target != null) {
            target.click();
        }
    }

    @Step("Select search-top category: {categoryTitle}")
    public void selectSearchTopCategory(String categoryTitle) {
        if (!categoryContainer.isVisible()) {
            clickSearchField();
        }
        String itemXp = String.format(dynamicCategoryByTitle, categoryTitle);
        categoryContainer.getChildElement(itemXp).click();
    }

    @Step("Select search-top category by enum: {category}")
    public void selectSearchTopCategory(Category category) {
        if (!categoryContainer.isVisible()) {
            clickSearchField();
            if (!categoryContainer.isVisible()) {
                return;
            }
        }
        String byTitleXp = String.format(dynamicCategoryByTitle, category.getTitle());
        String byHrefXp = String.format(dynamicCategoryByHref, category.getHref());
        categoryContainer.waitForDisplay();
        WebElement target = categoryContainer.getChildElement(byTitleXp);
        if (target == null) {
            target = categoryContainer.getChildElement(byHrefXp);
        }
        if (target != null) {
            target.click();
        }
    }
}

