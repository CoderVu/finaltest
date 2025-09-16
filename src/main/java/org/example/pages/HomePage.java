package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.common.Constants;
import org.example.enums.Category;
import org.example.core.control.common.imp.Element;
import org.example.core.control.factory.ElementFactory;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.*;
import static org.example.core.control.util.DriverUtils.getCurrentUrl;

public class HomePage extends BasePage {

    public HomePage() { super(); }

    public HomePage(ElementFactory factory) { super(factory); }

    // Controls
    protected final Element searchField = ui.elementByCss("input[data-view-id='main_search_form_input'][placeholder='Freeship đơn từ 45k']");
    protected final Element categoryContainer = ui.elementByXpath("//div[@data-view-id='search_top.category_product_container']");
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
        $(By.cssSelector("input[data-view-id='main_search_form_input'][placeholder='Freeship đơn từ 45k']")).shouldBe(Condition.visible);
        searchField.click();
    }


    @Step("Enter search text: {text}")
    public void setSearchText(String text) {
        $(By.cssSelector("input[data-view-id='main_search_form_input'][placeholder='Freeship đơn từ 45k']")).shouldBe(Condition.visible);
        searchField.setValue(text);
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
        if (!$(By.xpath("//div[@data-view-id='search_top.category_product_container']")).is(Condition.visible)) {
            clickSearchField();
            if (!$(By.xpath("//div[@data-view-id='search_top.category_product_container']")).is(Condition.visible)) {
                return; // container still not visible, skip
            }
        }
        SelenideElement container = $(By.xpath("//div[@data-view-id='search_top.category_product_container']"));
        String itemXp = String.format(dynamicCategoryByTitle, categoryTitle);
        container.$x(itemXp).shouldBe(Condition.visible).click();
    }

    @Step("Select search-top category by enum: {category}")
    public void selectSearchTopCategory(Category category) {
        if (!$(By.xpath("//div[@data-view-id='search_top.category_product_container']")).is(Condition.visible)) {
            clickSearchField();
            if (!$(By.xpath("//div[@data-view-id='search_top.category_product_container']")).is(Condition.visible)) {
                return; // container still not visible, skip
            }
        }
        SelenideElement container = $(By.xpath("//div[@data-view-id='search_top.category_product_container']"));
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

