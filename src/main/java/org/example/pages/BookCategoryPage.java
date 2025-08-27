package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.enums.Breadcrumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.codeborne.selenide.Selenide.*;

public class BookCategoryPage extends BasePage {

    private static final Logger log = LoggerFactory.getLogger(BookCategoryPage.class);

    // Locators
    private final SelenideElement breadcrumb = $("#breadcrumb, .breadcrumb");
    private final SelenideElement allProductsSection = $("#all-products, .sc-dc63c727-1"); 
    private final SelenideElement filterAllButton = $("#filter-all, button[title='Tất cả bộ lọc'], .sc-bd134f7-0:contains('Tất cả')"); 
    private final SelenideElement filterDialogTitle = $(".title:contains('Tất cả bộ lọc'), h4:contains('Tất cả bộ lọc')");
    
    // Updated locators for supplier section based on actual HTML structure
    private final SelenideElement supplierSection = $("[data-view-label='Nhà cung cấp'], .sc-63e2c595-0:has(.title:contains('Nhà cung cấp'))");
    private final SelenideElement fahasaSupplierCheckbox = supplierSection.$("label:contains('Nhà sách Fahasa'), .item--seller:contains('Nhà sách Fahasa')");
    
    // Alternative price input locators for the custom price range section
    private final SelenideElement priceSection = $("[data-view-label='Giá'], .sc-63e2c595-0:has(.title:contains('Giá'))");
    private final SelenideElement priceRangeMinInput = priceSection.$("input[pattern='[0-9]*'][placeholder='Từ'], input[placeholder='Từ']"); 
    private final SelenideElement priceRangeMaxInput = priceSection.$("input[pattern='[0-9]*'][placeholder='Đến'], input[placeholder='Đến']"); 
    private final SelenideElement viewResultButton = $("#view-result, button:contains('Xem Kết quả'), button:contains('Áp dụng')"); 
    private final ElementsCollection productPrices = $$(".product-item .price, .price-current, .price-discount__price"); 
    private final SelenideElement selectedSupplierLabel = $("#selected-supplier, .selected-filter:contains('Nhà sách Fahasa'), .filter-applied:contains('Fahasa')"); 

  @Step("Verify breadcrumb navigation matches expected: {expectedBreadcrumb}")
    public boolean verifyBreadcrumb(Breadcrumb expectedBreadcrumb) {
        String expectedText = expectedBreadcrumb.getFullBreadcrumb();

        String breadcrumbText = breadcrumb
                .shouldBe(Condition.exist)
                .getText()
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return breadcrumbText.equals(expectedText);
    }

    @Step("Click on 'Tất cả' button under 'Tất cả sản phẩm' section")
    public void clickAllFiltersButton() {
        allProductsSection.shouldBe(Condition.visible);
        
        // Try multiple possible selectors for the "Tất cả" filter button
        try {
            filterAllButton.shouldBe(Condition.visible).click();
        } catch (Exception e) {
            // Alternative approaches if the primary locator fails
            try {
                $(".sc-bd134f7-0:contains('Tất cả')").shouldBe(Condition.visible).click();
            } catch (Exception e2) {
                // Last resort - look for any filter button
                $("button:contains('Tất cả'), .filter-button:contains('Tất cả')").shouldBe(Condition.visible).click();
            }
        }
    }

    @Step("Verify that 'Tất cả bộ lọc' dialog is displayed")
    public boolean verifyFilterDialogDisplayed() {      
        try {
            filterDialogTitle.shouldBe(Condition.visible);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Check on supplier 'Nhà sách Fahasa' checkbox")
    public void selectSupplierFahasa() {
        log.info("Selecting supplier 'Nhà sách Fahasa'");
        
        // Wait for the supplier section to be visible
        supplierSection.shouldBe(Condition.visible);
        
        // Try multiple ways to find and click the Fahasa checkbox
        try {
            fahasaSupplierCheckbox.shouldBe(Condition.visible).click();
        } catch (Exception e) {
            // Alternative approach - find it within the supplier section by different selectors
            try {
                supplierSection.$("label[data-view-index='4']").shouldBe(Condition.visible).click();
            } catch (Exception e2) {
                // Last resort - find by partial text match
                supplierSection.$(".item:contains('Fahasa')").shouldBe(Condition.visible).click();
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
                $("button:contains('Xem')").shouldBe(Condition.visible).click();
            } catch (Exception e2) {
                // Last resort - find any button that might apply filters
                $("button:contains('Áp dụng'), .btn-apply, .apply-button").shouldBe(Condition.visible).click();
            }
        }
        
        log.info("Clicked 'Xem Kết quả' button");
    }

    @Step("Verify supplier 'Nhà sách Fahasa' is highlighted")
    public boolean verifySupplierHighlighted() {
        log.info("Verifying supplier 'Nhà sách Fahasa' is highlighted");
        
        // Wait a moment for filters to be applied
        sleep(2000);
        
        // Check if selected supplier label exists and is visible
        if (!selectedSupplierLabel.exists()) {
            log.warn("Selected supplier label does not exist");
            return false;
        }
        
        if (!selectedSupplierLabel.isDisplayed()) {
            log.warn("Selected supplier label is not visible");
            return false;
        }
        
        log.info("Supplier 'Nhà sách Fahasa' is highlighted");
        return true;
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
