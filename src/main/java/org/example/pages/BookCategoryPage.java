package org.example.pages;

import org.example.enums.Breadcrumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

import java.util.Arrays;
import java.util.List;

import com.codeborne.selenide.SelenideElement;

import io.qameta.allure.Step;

public class BookCategoryPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(BookCategoryPage.class);

    // Locators
    private final SelenideElement breadcrumb = $("#breadcrumb, .breadcrumb");
    private final SelenideElement allProductsSection = $("#all-products, .sc-dc63c727-1"); 
    private final SelenideElement filterAllButton = $("#filter-all, button[title='Tất cả bộ lọc']"); 
    private final SelenideElement filterDialogTitle = $(".title");
    
    // Updated locators for supplier section based on actual HTML structure
    private final SelenideElement supplierSection = $("[data-view-label='Nhà cung cấp']");
    private final SelenideElement fahasaSupplierCheckbox = supplierSection.$("label");
    
    // Alternative price input locators for the custom price range section
    private final SelenideElement priceSection = $("[data-view-label='Giá']");
    private final SelenideElement priceRangeMinInput = $("input[placeholder='Từ']"); 
    private final SelenideElement priceRangeMaxInput = $("input[placeholder='Đến']"); 
    private final SelenideElement viewResultButton = $("#view-result"); 
    private final ElementsCollection productPrices = $$(".product-item .price, .price-current, .price-discount__price"); 
    private final SelenideElement selectedSupplierLabel = $("#selected-supplier"); 

    @Step("Verify breadcrumb navigation matches expected: {expectedBreadcrumb}")
    public boolean verifyBreadcrumb(Breadcrumb expectedBreadcrumb) {
        String breadcrumbText = breadcrumb.shouldBe(Condition.exist).getText().trim();
        List<String> actualItems = Arrays.asList(breadcrumbText.split("\\s+"));
        List<String> expectedItems = Arrays.asList(
                expectedBreadcrumb.getHomeText(),
                expectedBreadcrumb.getCategoryText()
        );

        return actualItems.equals(expectedItems);
    }

    @Step("Click on 'Tất cả' button under 'Tất cả sản phẩm' section")
    public void clickAllFiltersButton() {
        allProductsSection.shouldBe(Condition.visible);
        
        // Try multiple possible selectors for the "Tất cả" filter button
        try {
            // First try the primary selector
            filterAllButton.shouldBe(Condition.visible).click();
        } catch (Exception e) {
            // Try to find by class name that might contain "Tất cả"
            try {
                $$(".sc-bd134f7-0").findBy(Condition.text("Tất cả")).click();
            } catch (Exception e2) {
                // Try to find any button with "Tất cả" text
                try {
                    $$("button").findBy(Condition.text("Tất cả")).click();
                } catch (Exception e3) {
                    // Last resort - look for buttons containing "Tất"
                    $$("button").findBy(Condition.partialText("Tất")).click();
                }
            }
        }
    }

    @Step("Verify that 'Tất cả bộ lọc' dialog is displayed")
    public boolean verifyFilterDialogDisplayed() {      
        try {
            // Check if filter dialog title is visible
            filterDialogTitle.shouldBe(Condition.visible);
            return true;
        } catch (Exception e) {
            // Try alternative ways to detect the filter dialog
            try {
                // Look for any title containing "Tất cả bộ lọc"
                $$(".title").findBy(Condition.text("Tất cả bộ lọc")).shouldBe(Condition.visible);
                return true;
            } catch (Exception e2) {
                // Look for h4 elements containing the filter text
                try {
                    $$("h4").findBy(Condition.text("Tất cả bộ lọc")).shouldBe(Condition.visible);
                    return true;
                } catch (Exception e3) {
                    return false;
                }
            }
        }
    }

    @Step("Check on supplier 'Nhà sách Fahasa' checkbox")
    public void selectSupplierFahasa() {
        log.info("Selecting supplier 'Nhà sách Fahasa'");
        
        // Wait for the supplier section to be visible
        supplierSection.shouldBe(Condition.visible);
        
        // Try multiple ways to find and click the Fahasa checkbox
        try {
            // Look for the specific supplier label with Fahasa text
            supplierSection.$$("label").findBy(Condition.text("Nhà sách Fahasa")).click();
        } catch (Exception e) {
            // Alternative approach - find it by data-view-index="4" as seen in HTML
            try {
                supplierSection.$("label[data-view-index='4']").shouldBe(Condition.visible).click();
            } catch (Exception e2) {
                // Last resort - find by partial text match
                try {
                    supplierSection.$$(".item").findBy(Condition.partialText("Fahasa")).click();
                } catch (Exception e3) {
                    // Very last resort - find any label containing "Fahasa"
                    supplierSection.$$("label").findBy(Condition.partialText("Fahasa")).click();
                }
            }
        }
        
        log.info("Selected supplier 'Nhà sách Fahasa'");
    }

    @Step("Enter price range from {minPrice} to {maxPrice}")
    public void enterPriceRange(String minPrice, String maxPrice) {
        log.info("Entering price range: {} - {}", minPrice, maxPrice);
        
        priceRangeMinInput.shouldBe(Condition.visible).setValue(minPrice);
        priceRangeMaxInput.shouldBe(Condition.visible).setValue(maxPrice);
        
        log.info("Price range entered successfully");
    }

    @Step("Enter price range from {minPrice} to {maxPrice}")
    public void enterPriceRange(int minPrice, int maxPrice) {
        enterPriceRange(String.valueOf(minPrice), String.valueOf(maxPrice));
    }

    @Step("Click 'Xem Kết quả' button")
    public void clickViewResultButton() {
        log.info("Clicking 'Xem Kết quả' button");
        
        // Try multiple possible locations for the button
        try {
            viewResultButton.shouldBe(Condition.visible).click();
        } catch (Exception e) {
            // If the primary locator fails, try alternative selectors
            try {
                $$("button").findBy(Condition.text("Xem Kết quả")).click();
            } catch (Exception e2) {
                // Try partial text match
                try {
                    $$("button").findBy(Condition.partialText("Xem")).click();
                } catch (Exception e3) {
                    // Last resort - find any button that might apply filters
                    $$("button").findBy(Condition.text("Áp dụng")).click();
                }
            }
        }
        
        log.info("Clicked 'Xem Kết quả' button");
    }

    @Step("Verify supplier 'Nhà sách Fahasa' is highlighted")
    public boolean verifySupplierHighlighted() {
        log.info("Verifying supplier 'Nhà sách Fahasa' is highlighted");
        
        // Wait a moment for filters to be applied
        sleep(2000);
        
        // Try multiple ways to verify supplier is selected
        try {
            // Check if selected supplier label exists and is visible
            selectedSupplierLabel.shouldBe(Condition.visible);
            return true;
        } catch (Exception e) {
            // Alternative check - look for any element indicating Fahasa is selected
            try {
                $$(".selected-filter").findBy(Condition.partialText("Fahasa")).shouldBe(Condition.visible);
                return true;
            } catch (Exception e2) {
                // Check for filter applied indicators
                try {
                    $$(".filter-applied").findBy(Condition.partialText("Fahasa")).shouldBe(Condition.visible);
                    return true;
                } catch (Exception e3) {
                    log.warn("No evidence found that supplier 'Nhà sách Fahasa' is highlighted");
                    return false;
                }
            }
        }
    }

    @Step("Apply filters")
    public void applyFilters() {
        log.info("Applying filters by clicking 'Xem Kết quả' button");
        clickViewResultButton();
    }

    @Step("Verify supplier is selected/highlighted")
    public boolean verifySupplierSelected() {
        return verifySupplierHighlighted();
    }

    @Step("Verify all product prices are within range {minPrice} - {maxPrice}")
    public boolean verifyProductPricesInRange(int minPrice, int maxPrice) {
        log.info("Verifying all product prices are within range: {}đ - {}đ", minPrice, maxPrice);
        
        // Wait for products to load
        sleep(3000);
        
        // Check if product prices collection exists
        if (productPrices.size() == 0) {
            log.warn("No product prices found on the page");
            return false;
        }
        
        log.info("Found {} price elements", productPrices.size());
        
        int checkedPrices = 0;
        for (SelenideElement priceElement : productPrices) {
            if (priceElement.exists() && priceElement.isDisplayed()) {
                String priceText = priceElement.getText().trim();
                log.debug("Raw price text: '{}'", priceText);
                
                // Extract numeric value from price text
                String numericPrice = priceText.replaceAll("[^0-9]", "");
                
                if (!numericPrice.isEmpty()) {
                    int price = Integer.parseInt(numericPrice);
                    log.debug("Checking price: {}đ", price);
                    checkedPrices++;
                    
                    if (price < minPrice || price > maxPrice) {
                        log.error("❌ Price {}đ is NOT within range {}đ - {}đ", price, minPrice, maxPrice);
                        return false;
                    } else {
                        log.debug("✅ Price {}đ is within range {}đ - {}đ", price, minPrice, maxPrice);
                    }
                }
            }
        }
        
        if (checkedPrices == 0) {
            log.warn("No valid prices were found to verify");
            return false;
        }
        
        log.info("All {} product prices are within the specified range", checkedPrices);
        return true;
    }

    @Step("Apply filters with price range {minPrice} - {maxPrice}")
    public void applyFiltersWithPriceRange(String minPrice, String maxPrice) {
        clickAllFiltersButton();
        verifyFilterDialogDisplayed();
        selectSupplierFahasa();
        enterPriceRange(minPrice, maxPrice);
        clickViewResultButton();
    }
}
