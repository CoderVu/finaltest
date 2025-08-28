package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.CollectionCondition;

import java.time.Duration;
import java.util.*;
import java.math.BigDecimal;

import com.codeborne.selenide.SelenideElement;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.example.enums.BreadcrumbToCategory;
import org.example.models.Product;

import static com.codeborne.selenide.Selenide.*;
import static java.util.Collections.emptyList;
import static org.example.utils.FormatUtils.formatPrice;
import static org.example.utils.FormatUtils.normalize;

@Slf4j
public class ShopPage extends BasePage {

    // Locators
    protected final SelenideElement breadcrumb = $x("//div[contains(@class,'breadcrumb')]");
    protected final SelenideElement allProductsSection = $(".sc-dc63c727-1");
    protected final SelenideElement filterAllButton = $x("//button[.//div[normalize-space(text())='Tất cả']]");
    protected final SelenideElement filterDialogTitle = $(".title");

    protected final SelenideElement priceRangeMinInput = $("input[placeholder='Từ']");
    protected final SelenideElement priceRangeMaxInput = $("input[placeholder='Đến']");
    protected final SelenideElement viewResultButton = $("#view-result");
    protected final ElementsCollection productCards = $$x("//a[contains(@class,'product-item')]");
    protected final ElementsCollection productPrices = $$x("//a[contains(@class,'product-item')]//*[self::div or self::span][contains(@class,'price-discount__price') or contains(@class,'price-current')][1]");
    protected final SelenideElement loadMoreButton = $x("//div[@data-view-id='category_infinity_view.more']");
    protected final SelenideElement selectedSupplierLabel = $("#selected-supplier");
    protected final ElementsCollection actualItemsXpath = $$x(".//a[contains(@class,'breadcrumb-item')]//span");
    protected static final SelenideElement filled = $x(".//div[@style and contains(@style,'width')]");

    protected static final String actionsContainerXPath = "//div[contains(@class,'sc-add2a4bc-5')]";

    // Dynamic locators
    protected static final String dynamicSectionLabel = "//div[@data-view-label='%s']";
    protected static final String dynamicOptionText = ".//label[.//span[normalize-space(text())='%s']]";
    protected static final String dynamicActionText = ".//div[normalize-space(text())='%s']";

    // Pill highlight locators (top-defined, to be called below)
    protected static final String sectionPillContainerByTitle = "//div[.//div[normalize-space(text())='%s']]";
    protected static final String pillButtonByTextActive = ".//button[contains(@class,'filter-child') and .//div[normalize-space(text())='%s'] and contains(@class,'izfbuI')]";
    protected static final String pillButtonActiveText = ".//button[contains(@class,'filter-child') and contains(@class,'izfbuI')]//div";

    // Product card relative locators
    protected static final String productCardNameRel = ".//h3";
    protected static final String productCardPriceRel = ".//*[contains(@class,'price-discount__price') or contains(@class,'price-current')][1]";
    protected static final String productCardImageRel = ".//img";
    protected static final String productCardLinkRel = ".//a[contains(@class,'product-item')]|self::a[contains(@class,'product-item')]";
    protected static final String productCardRatingWrapperRel = ".//div[contains(@class,'sc-980e9960-0')]";

    // Viewed history section locators
    protected static final String viewedHistorySectionTitleXp = "//h2[normalize-space(text())='Sản phẩm đã xem']";
    protected static final String viewedHistorySectionRootFromTitleXp = "//div[@data-view-id='product_list_recently_view_container']";
    protected static final String viewedHistoryItemRel = ".//a[@data-view-id='product_list_recently_view_item']";
    protected static final String viewedHistoryNameRel = ".//h3";
    protected static final String viewedHistoryPriceRel = ".//p[contains(@class,'price')]//span[1]";
    protected static final String viewedHistoryImageRel = ".//img";


    @Step("Verify breadcrumb navigation matches expected: {expectedBreadcrumb}")
    public boolean verifyBreadcrumbToCategory(BreadcrumbToCategory expectedBreadcrumb) {
        breadcrumb.shouldBe(Condition.exist);
        List<String> actualItems = actualItemsXpath.texts();
        List<String> expectedItems = Arrays.asList(
                expectedBreadcrumb.getHomeText(),
                expectedBreadcrumb.getCategoryText()
        );
        return actualItems.equals(expectedItems);
    }

    @Step("Click on 'Tất cả' button under 'Tất cả sản phẩm' section")
    public void clickAllFiltersButton() {
        allProductsSection.shouldBe(Condition.visible);
        filterAllButton.shouldBe(Condition.visible).click();
    }

    @Step("Check supplier '{optionText}' is highlighted in section '{sectionLabel}'")
    public boolean isSupplierHighlighted(String sectionLabel, String optionText) {
        return isHighlightedPillBlue(sectionLabel, optionText);
    }

    private BigDecimal parsePriceToNumber(String rawPriceText) {
        if (rawPriceText == null) {
            return BigDecimal.ZERO;
        }
        String digitsOnly = rawPriceText.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digitsOnly);
    }

    @Step("Collect displayed products from grid")
    public List<Product> getDisplayedProducts() {
        waitForSizeProductGreaterThan(0);
        List<Product> products = new ArrayList<>();
        for (SelenideElement card : productCards) {
            String idStr = card.getAttribute("data-view-content");
            Long id = null;
            try {
                String digits = idStr == null ? null : idStr.replaceAll("[^0-9]", "");
                if (digits != null && !digits.isEmpty()) {
                    id = Long.parseLong(digits);
                }
            } catch (Exception ignored) {}

            String name = card.$x(productCardNameRel).exists() ? card.$x(productCardNameRel).getText() : "";
            String priceText = card.$x(productCardPriceRel).exists()
                    ? card.$x(productCardPriceRel).getText() : "";
            String img = card.$x(productCardImageRel).exists() ? card.$x(productCardImageRel).getAttribute("src") : "";

            products.add(Product.builder()
                    .id(id)
                    .productName(name)
                    .productPrice(priceText)
                    .productImage(img)
                    .build());
        }
        return products;
    }

    @Step("Wait for product cards to appear")
    public void waitForSizeProductGreaterThan(Integer quantity) {
        productCards.shouldHave(CollectionCondition.sizeGreaterThan(quantity), Duration.ofSeconds(10));
        productCards.first().shouldBe(Condition.visible);
    }

    @Step("Is 'Xem thêm' button visible")
    public boolean isLoadMoreVisible() {
        return loadMoreButton.exists() && loadMoreButton.is(Condition.visible);
    }

        @Step("Click 'Xem thêm' up to {maxClicks} times to load more products")
    public void loadMoreProducts(int maxClicks) {
        int clicks = 0;
        int previousCount = productCards.size();
        while (clicks < maxClicks) {
            if (!isLoadMoreVisible()) {
                break;
            }
            loadMoreButton.scrollTo().shouldBe(Condition.interactable).click();
            waitForSizeProductGreaterThan(previousCount);
            productCards.first().shouldBe(Condition.visible);
            int newCount = productCards.size();
            if (newCount <= previousCount) {
                break;
            }
            previousCount = newCount;
            clicks++;
            if (!isLoadMoreVisible()) {
                break;
            }
        }
    }

    public enum ViewType { GRID, LIST, UNKNOWN }

    @Step("Detect product view type (grid vs list)")
    public ViewType getProductViewType() {
        if (productCards == null || productCards.isEmpty()) {
            Allure.step("No product cards found - ViewType: UNKNOWN");
            return ViewType.UNKNOWN;
        }
        int numberItems = productCards.size();
        int Size = Math.min(productCards.size(), numberItems);
        
        for (int i = 0; i < Size; i++) {
            productCards.get(i).shouldBe(Condition.visible);
        }
        
        List<Integer> yPositions = new ArrayList<>();
        

        for (int i = 0; i < Size; i++) {
            SelenideElement card = productCards.get(i);
            int y = card.getLocation().getY();
            yPositions.add(y);
        }
        
        Collections.sort(yPositions);
        
        List<List<Integer>> rowGroups = new ArrayList<>();
        List<Integer> currentRow = new ArrayList<>();
        int threshold = 20; 
        
        for (int y : yPositions) {
            if (currentRow.isEmpty() || Math.abs(y - currentRow.get(currentRow.size() - 1)) <= threshold) {
                currentRow.add(y);
            } else {
                rowGroups.add(new ArrayList<>(currentRow));
                currentRow.clear();
                currentRow.add(y);
            }
        }
        if (!currentRow.isEmpty()) {
            rowGroups.add(currentRow);
        }
        
        int totalRows = rowGroups.size();
        int maxPerRow = rowGroups.stream().mapToInt(List::size).max().orElse(1);
        
        ViewType viewType;
        if (maxPerRow > 1) {
            viewType = ViewType.GRID;
        } else if (maxPerRow == 1 && Size >= 2) {
            viewType = ViewType.LIST;
        } else {
            viewType = ViewType.UNKNOWN;
        }
        
        StringBuilder rowDetails = new StringBuilder();
        for (int i = 0; i < rowGroups.size(); i++) {
            rowDetails.append(String.format("Row %d: %d items; ", i + 1, rowGroups.get(i).size()));
        }
        
        Allure.step(String.format("Detected ViewType: %s - %d items per row (max), %d rows total. %s", 
                                  viewType, maxPerRow, totalRows, rowDetails.toString()));
        
        return viewType;
    }

    @Step("Is product view grid")
    public boolean isGridView() {
        ViewType viewType = getProductViewType();
        boolean isGrid = viewType == ViewType.GRID;
        Allure.step(String.format("Product view is grid: %s", isGrid));
        return isGrid;
    }

    @Step("Is product view list")
    public boolean isListView() {
        return getProductViewType() == ViewType.LIST;
    }

    @Step("Verify all products in grid have price within {minPrice}-{maxPrice}")
    public boolean areAllProductModelPricesWithin(double minPrice, double maxPrice) {
        loadMoreProducts(3);
        BigDecimal min = BigDecimal.valueOf(minPrice);
        BigDecimal max = BigDecimal.valueOf(maxPrice);
        List<Product> products = getDisplayedProducts();
        log.info("Verifying prices for {} products are within {} - {}", products.size(), minPrice, maxPrice);
        if (products.isEmpty()) return false;
        boolean allOk = true;
        for (Product p : products) {
            String priceRaw = p.getProductPrice();
            if (priceRaw == null || priceRaw.trim().isEmpty()) {
                log.warn("Skip product without price: {}", p.getProductName());
                continue;
            }
            BigDecimal price = parsePriceToNumber(priceRaw);
            if (price.compareTo(min) < 0 || price.compareTo(max) > 0) {
                log.error("Out of range price: {} for product '{}' (raw='{}')", price, p.getProductName(), priceRaw);
                allOk = false;
            }
        }
        return allOk;
    }

    @Step("Select any product and open details, return Product model")
    public Product selectAnyProductAndOpenDetails() {
        waitForSizeProductGreaterThan(0);
        int size = productCards.size();
        int pick = Math.max(0, Math.min(size - 1, 10));
        SelenideElement card = null;
        for (int idx = pick; idx >= 0; idx--) {
            try {
                card = productCards.get(idx).shouldBe(Condition.visible);
                break;
            } catch (Throwable ignored) {
            }
        }
        if (card == null) {
            card = productCards.first().shouldBe(Condition.visible);
        }

        // Parse id from data-view-content JSON if available
        Long id = null;
        try {
            String idStr = card.getAttribute("data-view-content");
            String digits = idStr == null ? null : idStr.replaceAll("[^0-9]", "");
            if (digits != null && !digits.isEmpty()) {
                id = Long.parseLong(digits);
            }
        } catch (Exception ignored) {}

        String name = card.$x(productCardNameRel).exists() ? card.$x(productCardNameRel).getText() : "";
        String price = card.$x(productCardPriceRel).exists() ? card.$x(productCardPriceRel).getText() : "";
        String img = "";
        if (card.$x(productCardImageRel).exists()) {
            String src = card.$x(productCardImageRel).getAttribute("src");
            if (src == null || src.isEmpty()) {
                String srcset = card.$x(productCardImageRel).getAttribute("srcset");
                if (srcset != null && !srcset.trim().isEmpty()) {
                    String[] parts = srcset.trim().split("\\s+");
                    if (parts.length > 0) src = parts[0];
                }
            }
            img = src == null ? "" : src;
        }
        Double rating = extractRatingFromStars(card);

        SelenideElement link = card.$x(productCardLinkRel);
        (link.exists() ? link : card).scrollTo().click();
        log.info("Selected product: name='{}' price='{}' id={} imgPresent={} rating={}", name, price, id, (img != null && !img.isEmpty()), rating);
        return Product.builder()
                .id(id)
                .productName(name)
                .productPrice(price)
                .productImage(img)
                .rating(rating)
                .build();
    }

    @Step("Find viewed product by exact/partial name in history")
    public Product findViewedProductByName(String productName) {
        if (productName == null || productName.isEmpty()) return null;
        List<Product> history = getViewedHistoryProducts();
        if (history.isEmpty()) return null;
        String needle = normalize(productName);
        for (Product p : history) {
            String hay = normalize(p.getProductName());
            if (!hay.isEmpty() && (hay.equals(needle) || hay.contains(needle) || needle.contains(hay))) {
                return p;
            }
        }
        return null;
    }

    @Step("Collect viewed-history products (image, name, price)")
    public List<Product> getViewedHistoryProducts() {
        SelenideElement section = $x(viewedHistorySectionRootFromTitleXp);
        section.scrollTo().shouldBe(Condition.exist, Duration.ofSeconds(20));
        ElementsCollection items = section.$$x(viewedHistoryItemRel);
        if (items.size() > 0) {
            items.first().shouldBe(Condition.visible, Duration.ofSeconds(10));
        }
        List<Product> products = new ArrayList<>();
        for (SelenideElement item : items) {
            String name = item.$x(viewedHistoryNameRel).exists() ? item.$x(viewedHistoryNameRel).getText() : "";
            String price = item.$x(viewedHistoryPriceRel).exists() ? item.$x(viewedHistoryPriceRel).getText() : "";
            String img = item.$x(viewedHistoryImageRel).exists() ? item.$x(viewedHistoryImageRel).getAttribute("src") : "";
            Double rating = extractRatingFromStars(item);
            products.add(Product.builder()
                    .productName(name)
                    .productPrice(price)
                    .productImage(img)
                    .rating(rating)
                    .build());
        }
        return products;
    }


    private Double extractRatingFromStars(SelenideElement scope) {
        try {
            SelenideElement wrapper = scope.$x(productCardRatingWrapperRel);
            if (!wrapper.exists()) return null;
            ElementsCollection candidates = wrapper.$$x(".//div[@style]");
            for (SelenideElement el : candidates) {
                String style = el.getAttribute("style");
                if (style == null) continue;
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("width:\\s*([0-9]+)\\%").matcher(style);
                if (m.find()) {
                    int percent = Integer.parseInt(m.group(1));
                    double stars = percent / 20.0;
                    return Math.round(stars * 2.0) / 2.0;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Step("Check viewed-history contains product by name, price, image and rating")
    public boolean checkViewHistoryProduct(Product expected) {

//        waitForViewedHistoryToLoad();
        
        List<Product> history = getViewedHistoryProducts();
        if (history.isEmpty()) {
            log.warn("Viewed history is empty while checking expected product: name='{}' price='{}'", expected.getProductName(), expected.getProductPrice());
            return false;
        }
        log.info("Checking viewed history with {} items for expected product: name='{}' price='{}'", history.size(), expected.getProductName(), expected.getProductPrice());
        BigDecimal expectedPrice = parsePriceToNumber(expected.getProductPrice());
        for (Product p : history) {
            boolean nameMatch = p.getProductName() != null && expected.getProductName() != null
                    && p.getProductName().contains(expected.getProductName().substring(0, Math.min(15, expected.getProductName().length())));
            BigDecimal price = parsePriceToNumber(p.getProductPrice());
            boolean priceMatch = expectedPrice.compareTo(BigDecimal.ZERO) == 0 || price.compareTo(expectedPrice) == 0;
            boolean imagePresent = p.getProductImage() != null && !p.getProductImage().isEmpty();
            boolean ratingMatch = true;
            if (expected.getRating() != null) {
                Double hr = p.getRating();
                Double er = expected.getRating();
                ratingMatch = hr != null && Math.abs(hr - er) <= 0.25;
            }
            if (nameMatch && priceMatch && imagePresent && ratingMatch) {
                return true;
            }
        }
        return false;
    }
    
//    @Step("Wait for viewed history section to load with items")
//    private void waitForViewedHistoryToLoad() {
//        try {
//            SelenideElement section = $x(viewedHistorySectionRootFromTitleXp);
//            section.scrollTo().shouldBe(Condition.exist, Duration.ofSeconds(20));
//            int maxAttempts = 10;
//            int attempt = 0;
//            while (attempt < maxAttempts) {
//                ElementsCollection items = section.$$x(viewedHistoryItemRel);
//                if (items.size() > 0) {
//                    log.info("Viewed history section loaded with {} items", items.size());
//                    return;
//                }
//                log.info("Attempt {}: No items found in viewed history, waiting...", attempt + 1);
//                attempt++;
//            }
//            log.warn("Viewed history section did not load items after {} attempts", maxAttempts);
//        } catch (Exception e) {
//            log.error("Error waiting for viewed history to load: {}", e.getMessage());
//        }
//    }

    @Step("Select filter option: section='{sectionLabel}', option='{optionText}'")
    public void selectFilterOption(String sectionLabel, String optionText) {
        String sectionXPath = String.format(dynamicSectionLabel, sectionLabel);
        SelenideElement section = $x(sectionXPath).shouldBe(Condition.visible);
        String optionXPath = String.format(dynamicOptionText, optionText);
        SelenideElement option = section.$x(optionXPath);

        option.shouldBe(Condition.visible).click();
    }

    @Step("Enter price range from {minPrice} to {maxPrice}")
    public void enterPriceRange(double minPrice, double maxPrice) {
        setText(priceRangeMinInput, formatPrice(minPrice));
        setText(priceRangeMaxInput, formatPrice(maxPrice));
    }


    @Step("Click 'Xem Kết quả' button")
    public void clickViewResultButton() {
        clickFilterAction("Xem kết quả");
    }


    @Step("Apply filters with price range {minPrice} - {maxPrice}")
    public void applyFiltersWithPriceRange(double minPrice, double maxPrice) {
        enterPriceRange(minPrice, maxPrice);
        clickViewResultButton();
    }

    public boolean isFilterDialogTitleDisplayed() {
        return filterDialogTitle.is(Condition.visible);
    }

    @Step("Click filter action: {actionText}")
    public void clickFilterAction(String actionText) {
        SelenideElement actionsContainer = $x(actionsContainerXPath).shouldBe(Condition.visible);
        SelenideElement action = actionsContainer.$x(String.format(dynamicActionText, actionText));
        action.shouldBe(Condition.visible).click();
    }

    @Step("Get CSS color of highlighted pill '{optionText}' under section '{sectionLabel}'")
    public String getHighlightedPillColor(String sectionLabel, String optionText) {
        String container = String.format(sectionPillContainerByTitle, sectionLabel);
        String pill = String.format(pillButtonByTextActive, optionText);
        SelenideElement pillButton = $x(container).$x(pill).shouldBe(Condition.visible);
        SelenideElement innerText = pillButton.$x(".//div");
        String color = innerText.exists() ? innerText.getCssValue("color") : null;
        if (color == null || color.isEmpty()) {
            color = pillButton.getCssValue("color");
        }
        return color == null ? "" : color.trim();
    }

    private String normalizeCssColor(String cssColor) {
        if (cssColor == null) return "";
        return cssColor.replace(" ", "").toLowerCase();
    }

    @Step("Verify highlighted pill '{optionText}' is blue under section '{sectionLabel}'")
    public boolean isHighlightedPillBlue(String sectionLabel, String optionText) {
        String color = getHighlightedPillColor(sectionLabel, optionText);
        String norm = normalizeCssColor(color);
        return norm.equals("rgb(10,104,255)") || norm.equals("rgba(10,104,255,1)");
    }

}