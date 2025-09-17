package org.example.pages;

import lombok.extern.slf4j.Slf4j;
 

import java.time.Duration;
import java.util.*;
import java.math.BigDecimal;

 
import com.codeborne.selenide.WebDriverConditions;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.example.core.control.common.annotation.FindBy;
import org.example.enums.BreadcrumbToCategory;
import org.example.models.Product;
import org.example.enums.ViewType;
import static com.codeborne.selenide.Selenide.sleep;
import static org.example.core.control.util.DriverUtils.getCurrentUrl;
import static org.example.utils.FormatUtils.formatPrice;
import org.example.enums.Category;
import org.example.core.control.common.imp.Element;
import org.example.core.control.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


@Slf4j
public class ShopPage extends BasePage {

    public ShopPage() { super(); }


    // Locators
    @FindBy(xpath = "//div[contains(@class,'breadcrumb')]")
    protected Element breadcrumb;

    @FindBy(css = ".sc-dc63c727-1")
    protected Element allProductsSection;

    @FindBy(xpath = "//button[.//div[normalize-space(text())='Tất cả']]")
    protected Element filterAllButton;

    @FindBy(css = ".title")
    protected Element filterDialogTitle;

    @FindBy(css ="input[placeholder='Từ']")
    protected Element priceRangeMinInput;

    @FindBy(css ="input[placeholder='Đến']")
    protected Element priceRangeMaxInput;

    @FindBy(css = "#view-result")
    protected Element viewResultButton;

    @FindBy(xpath = "//a[contains(@class,'product-item')]")
    protected Element productCards;

    @FindBy(xpath = "//a[contains(@class,'product-sitem')]//*[self::div or self::span][contains(@class,'price-discount__price') or contains(@class,'price-current')][1]")
    protected Element productPrices;
    @FindBy(xpath = "//div[@data-view-id='category_infinity_view.more']")
    protected Element loadMoreButton;
    @FindBy(css = "#selected-supplier")
    protected Element selectedSupplierLabel;
    @FindBy(xpath = ".//div[@style and contains(@style,'width')]")
    protected Element filled;
    protected String h2ByExactText = "//h2[normalize-space(text())='%s']";

    // Rel/absolute xpaths as constants
    protected String togglerRel = ".//a[contains(@class,'toggler')]";
    protected String modalTogglerMoreRel = ".//a[contains(@class,'toggler') and normalize-space(text())='Xem thêm']";
    protected String actionsContainerXPath = "//div[contains(@class,'sc-add2a4bc-5')]";
    protected String breadcrumbItemsRel = ".//a[contains(@class,'breadcrumb-item')]//span";

    // Dynamic locators
    protected String dynamicSectionLabel = "//div[@data-view-label and .//h4[@class='title' and normalize-space(text())='%s']]";
    protected String dynamicOptionText = ".//*[self::label or self::div][(contains(@class,'item') or contains(@class,'filter-child2'))][.//span[normalize-space(text())='%s'] or .//div[normalize-space(text())='%s']]";
    protected String dynamicActionText = ".//div[normalize-space(text())='%s']";
    protected String dynamicLeftMenuCategory = "//div[contains(@class,'sc-36d678cb-6')]//a[contains(text(),'%s')]";

    // Pill highlight locators (top-defined, to be called below)
    protected String sectionPillContainerByTitle = "//div[@data-view-label='%s']";
    protected String pillButtonByTextActive = ".//button[contains(@class,'filter-child') and .//div[normalize-space(text())='%s'] and contains(@class,'izfbuI')]";
    protected String pillButtonActiveText = ".//button[contains(@class,'filter-child') and contains(@class,'izfbuI')]//div";
    protected String activePillAnywhereByText = "//button[contains(@class,'filter-child') and contains(@class,'izfbuI') and .//div[normalize-space(text())='%s']]";
    protected String activePillAnywhereByInnerDiv = "//div[contains(@class,'sc-bd134f7-2') and normalize-space(text())='%s']";
    protected String inlineFilterChild2CheckedExact = "//div[contains(@class,'filter-child2')][.//span[contains(@class,'box') and contains(@class,'checked')]]//*[self::span or self::div][normalize-space(text())='%s']";
    protected String inlineFilterChild2CheckedContains = "//div[contains(@class,'filter-child2')][.//span[contains(@class,'box') and contains(@class,'checked')]]//*[self::span or self::div][contains(normalize-space(.), '%s')]";
    protected String optionCheckboxCheckedRel = ".//span[contains(@class,'box') and contains(@class,'checked')]";
    protected String optionIconCheckOnRel = ".//img[contains(@class,'icon-check-on')]";

    // Product card relative locators
    protected String productCardNameRel = ".//h3";
    protected String productCardPriceRel = ".//*[contains(@class,'price-discount__price') or contains(@class,'price-current')][1]";
    protected String productCardImageRel = ".//img";
    protected String productCardLinkRel = ".//a[contains(@class,'product-item')]|self::a[contains(@class,'product-item')]";
    protected String productCardRatingWrapperRel = ".//div[contains(@class,'sc-980e9960-0')]";
    protected String productCardsXpath = "//a[contains(@class,'product-item')]";
    // Viewed history section locators
    protected String viewedHistorySectionTitleXp = "//h2[normalize-space(text())='Sản phẩm đã xem']";
    protected String viewedHistorySectionRootFromTitleXp = "//div[@data-view-id='product_list_recently_view_container']";
    protected String viewedHistoryItemRel = ".//a[@data-view-id='product_list_recently_view_item']";
    protected String viewedHistoryNameRel = ".//h3";
    protected String viewedHistoryPriceRel = ".//p[contains(@class,'price')]//span[1]";
    protected String viewedHistoryImageRel = ".//img";

    // Modal (All filters) locators
    protected String allFiltersModalRoot = "//div[contains(@class,'sc-add2a4bc-2')]";
    protected String modalSectionByTitle = ".//div[@data-view-id='search_filter_container' or @data-view-id='search_checkbox_filter_container'][.//h4[@class='title' and normalize-space(text())='%s']]";
    protected String modalItemByText = ".//*[self::label or self::div][(contains(@class,'item') or contains(@class,'filter-child2'))][.//span[normalize-space(text())='%s'] or .//div[normalize-space(text())='%s']]";
    protected String modalCheckedBoxRel = ".//span[contains(@class,'box') and contains(@class,'checked')]";

    // Left menu (category panel) locators
    protected String leftMenuRoot = "//div[contains(@class,'sc-36d678cb-1')]";
    protected String leftMenuMainCategoryAnchorByTitle = leftMenuRoot + "//a[.//div[contains(@class,'sc-36d678cb-3') and normalize-space(text())='%s']]";
    protected String leftMenuMainCategoryTitleByText = leftMenuRoot + "//div[contains(@class,'sc-36d678cb-3') and normalize-space(text())='%s']";
    protected String leftMenuSubItemsContainer = leftMenuRoot + "//div[contains(@class,'sc-36d678cb-5')]";
    protected String leftMenuSubItemRel = ".//div[contains(@class,'sc-36d678cb-6')]";
    protected String leftMenuTogglerMore = leftMenuRoot + togglerRel;

    @Step("Verify modal selection: section='{sectionTitle}', option='{optionText}' is checked")
    public boolean isModalOptionChecked(String sectionTitle, String optionText) {
        WebElement modal = DriverUtils.getWebDriver().findElement(By.xpath(allFiltersModalRoot));
        WebElement section = modal.findElement(By.xpath(String.format(modalSectionByTitle, sectionTitle)));
        WebElement item = findFirst(section, String.format(modalItemByText, optionText, optionText));
        if (item == null) {
            WebElement more = findFirst(section, modalTogglerMoreRel);
            if (more != null) more.click();
            item = findFirst(section, String.format(modalItemByText, optionText, optionText));
        }
        if (item == null) return false;
        boolean checked = !item.findElements(By.xpath(modalCheckedBoxRel)).isEmpty();
        if (!checked) {
            checked = !item.findElements(By.xpath(modalCheckedBoxRel)).isEmpty();
        }
        return checked;
    }

    @Step("Verify breadcrumb navigation matches expected: {expectedBreadcrumb}")
    public boolean verifyBreadcrumbToCategory(BreadcrumbToCategory expectedBreadcrumb) {
        breadcrumb.waitForDisplay();
        List<WebElement> items = breadcrumb.getChildElements(breadcrumbItemsRel);
        List<String> actualItems = new ArrayList<>();
        for (WebElement it : items) {
            actualItems.add(it.getText());
        }
        List<String> expectedItems = Arrays.asList(
                expectedBreadcrumb.getHomeText(),
                expectedBreadcrumb.getCategoryText()
        );
        return actualItems.equals(expectedItems);
    }

    @Step("Verify H2 page title displayed: {title}")
    public boolean isH2TitleDisplayed(String title) {
        String xp = String.format(h2ByExactText, title);
        return !DriverUtils.getWebDriver().findElements(By.xpath(xp)).isEmpty();
    }

    @Step("Click on 'Tất cả' button under 'Tất cả sản phẩm' section")
    public void clickAllFiltersButton() {
        filterAllButton.click(3);
        filterDialogTitle.waitForDisplay();
    }

    @Step("Select left menu category: {categoryName}")
    public void selectLeftMenuCategory(String categoryName) {
        String leftMenuXPath = String.format(dynamicLeftMenuCategory, categoryName);
        DriverUtils.getWebDriver().findElement(By.xpath(leftMenuXPath)).click();
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
        for (org.openqa.selenium.WebElement card : productCards.getElements()) {
            String idStr = card.getAttribute("data-view-content");
            Long id = null;
            try {
                String digits = idStr == null ? null : idStr.replaceAll("[^0-9]", "");
                if (digits != null && !digits.isEmpty()) {
                    id = Long.parseLong(digits);
                }
            } catch (Exception ignored) {
            }

            String name = findIfExistsText(card, productCardNameRel);
            String priceText = findIfExistsText(card, productCardPriceRel);
            String img = findIfExistsAttr(card, productCardImageRel, "src");

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
    public void waitForSizeProductGreaterThan(Integer moreThanQuantity) {
        long end = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
        while (System.currentTimeMillis() < end) {
            if (productCards.getElements().size() > moreThanQuantity) break;
            sleep(200);
        }
        // No-op wait here; wrapper polling above ensures presence
    }

    @Step("Is 'Xem thêm' button visible")
    public boolean isLoadMoreVisible() {
        return loadMoreButton.isExist() && loadMoreButton.isVisible();
    }

    @Step("Click 'Xem thêm' up to {maxClicks} times to load more products")
    public void loadMoreProducts(int maxClicks) {
        int clicks = 0;
        int previousCount = productCards.getElements().size();
        while (clicks < maxClicks) {
            if (!isLoadMoreVisible()) {
                break;
            }
            loadMoreButton.click();
            waitForSizeProductGreaterThan(previousCount);
            // ensure at least first card present
            int newCount = productCards.getElements().size();
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

    @Step("Click 'Xem thêm' until all products are loaded or no more growth")
    public void loadMoreProductsAll() {
        int safety = 30;
        int previousCount = -1;
        while (safety-- > 0) {
            if (!isLoadMoreVisible()) break;
            int before = productCards.getElements().size();
            try {
                loadMoreButton.click();
            } catch (Throwable ignored) {
                break;
            }
            waitForSizeProductGreaterThan(before);
            int after = productCards.getElements().size();
            if (after <= before || after == previousCount) {
                break;
            }
            previousCount = after;
        }
    }

    @Step("Wait for product grid to stabilize")
    private void waitForGridStability(int maxAttempts, long sleepMillis) {
        int prevSize = -1;
        String prevFirstHref = null;
        for (int i = 0; i < maxAttempts; i++) {
            int size;
            String href = null;
            try {
                List<WebElement> cards = productCards.getElements();
                size = cards.size();
                if (size > 0) {
                    WebElement firstEl = cards.get(0);
                    href = firstEl.getAttribute("href");
                }
            } catch (Throwable ignored) {
                size = -1;
            }
            if (size == prevSize && (href == null || href.equals(prevFirstHref))) {
                break;
            }
            prevSize = size;
            prevFirstHref = href;
            sleep(sleepMillis);
        }
    }

    @Step("Detect product view type (grid vs list)")
    public ViewType getProductViewType() {
        waitForSizeProductGreaterThan(0);
        waitForGridStability(10, 200);

        int retries = 3;
        while (retries-- > 0) {
            try {
                List<WebElement> cards = productCards.getElements();
                if (cards.isEmpty()) {
                    Allure.step("No product cards found - ViewType: UNKNOWN");
                    return ViewType.UNKNOWN;
                }

                int elementHeight = cards.get(0).getSize().getHeight();
                int threshold = Math.max(1, elementHeight / 2);

                java.util.Map<Integer, Integer> rowCountByY = new java.util.HashMap<>();
                for (int i = 0; i < cards.size(); i++) {
                    int y;
                    try {
                        y = cards.get(i).getLocation().getY();
                    } catch (Throwable stale) {
                        throw stale;
                    }
                    Integer matchedY = rowCountByY.keySet().stream()
                            .filter(existingY -> Math.abs(existingY - y) <= threshold)
                            .findFirst()
                            .orElse(y);
                    rowCountByY.put(matchedY, rowCountByY.getOrDefault(matchedY, 0) + 1);
                }

                int totalRows = rowCountByY.size();
                int maxPerRow = rowCountByY.values().stream().max(Integer::compareTo).orElse(1);

                ViewType viewType;
                if (maxPerRow > 1) {
                    viewType = ViewType.GRID;
                } else if (maxPerRow == 1 && cards.size() >= 2) {
                    viewType = ViewType.LIST;
                } else {
                    viewType = ViewType.UNKNOWN;
                }

                StringBuilder rowDetails = new StringBuilder();
                int rowIndex = 1;
                for (int count : rowCountByY.values()) {
                    rowDetails.append(String.format("Row %d: %d items; ", rowIndex++, count));
                }

                Allure.step(String.format(
                        "Detected ViewType: %s - %d items per row (max), %d rows total. %s",
                        viewType, maxPerRow, totalRows, rowDetails
                ));

                return viewType;
            } catch (Throwable ignored) {
                sleep(300);
            }
        }
        return ViewType.UNKNOWN;
    }


    @Step("Check product view {viewType}")
    public boolean checkProductView(ViewType viewType) {
        return getProductViewType() == viewType;
    }

    @Step("Verify all products in grid have price within {minPrice}-{maxPrice}")
    public boolean areAllProductModelPricesWithin(double minPrice, double maxPrice) {
        loadMoreProductsAll();
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
        int size = productCards.getElements().size();
        int pick = Math.max(0, Math.min(size - 1, 10));
        WebElement card = null;
        for (int idx = pick; idx >= 0; idx--) {
            try {
                card = productCards.getElements().get(idx);
                break;
            } catch (Throwable ignored) {
            }
        }
        if (card == null && !productCards.getElements().isEmpty()) {
            card = productCards.getElements().get(0);
        }

        // Parse id from data-view-content JSON if available
        Long id = null;
        try {
            if (card == null) throw new IllegalStateException("No product card available");
            String idStr = card.getAttribute("data-view-content");
            String digits = idStr == null ? null : idStr.replaceAll("[^0-9]", "");
            if (digits != null && !digits.isEmpty()) {
                id = Long.parseLong(digits);
            }
        } catch (Exception ignored) {
        }

        if (card == null) {
            return Product.builder()
                    .id(null)
                    .productName("")
                    .productPrice("")
                    .productImage("")
                    .rating(null)
                    .build();
        }
        String name = findIfExistsText(card, productCardNameRel);
        String price = findIfExistsText(card, productCardPriceRel);
        String img = "";
        if (elementExists(card, productCardImageRel)) {
            String src = findIfExistsAttr(card, productCardImageRel, "src");
            if (src == null || src.isEmpty()) {
                String srcset = findIfExistsAttr(card, productCardImageRel, "srcset");
                if (srcset != null && !srcset.trim().isEmpty()) {
                    String[] parts = srcset.trim().split("\\s+");
                    if (parts.length > 0) src = parts[0];
                }
            }
            img = src == null ? "" : src;
        }
        Double rating = extractRatingFromStars(card);

        WebElement linkEl = elementExists(card, productCardLinkRel) ? card.findElement(By.xpath(productCardLinkRel)) : null;
        (linkEl != null ? linkEl : card).click();
        log.info("Selected product: name='{}' price='{}' id={} imgPresent={} rating={}", name, price, id, (img != null && !img.isEmpty()), rating);
        return Product.builder()
                .id(id)
                .productName(name)
                .productPrice(price)
                .productImage(img)
                .rating(rating)
                .build();
    }


    private Double extractRatingFromStars(WebElement scope) {
        try {
            WebElement wrapper = existsRel(scope, productCardRatingWrapperRel) ? scope.findElement(By.xpath(productCardRatingWrapperRel)) : null;
            if (wrapper == null) return null;
            List<WebElement> candidates = wrapper.findElements(By.xpath(".//div[@style]"));
            for (WebElement el : candidates) {
                String style = el.getAttribute("style");
                if (style == null) continue;
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("width:\\s*([0-9]+)\\%").matcher(style);
                if (m.find()) {
                    int percent = Integer.parseInt(m.group(1));
                    double stars = percent / 20.0;
                    return Math.round(stars * 2.0) / 2.0;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Step("Select filter option: section='{sectionLabel}', option='{optionText}'")
    public void selectFilterOption(String sectionLabel, String optionText) {
        String sectionXPath = String.format(dynamicSectionLabel, sectionLabel);
        WebElement section = DriverUtils.getWebDriver().findElement(By.xpath(sectionXPath));
        String optionXPath = String.format(dynamicOptionText, optionText, optionText);
        WebElement option = findFirst(section, optionXPath);
        if (option == null) {
            WebElement toggle = findFirst(section, togglerRel);
            if (toggle != null) {
                String toggleText = toggle.getText().trim();
                if (toggleText.equalsIgnoreCase("Xem thêm")) {
                    DriverUtils.getWebDriver().findElement(By.xpath(sectionXPath + togglerRel)).click();
                }
            }
            option = findFirst(section, optionXPath);
        }
        if (option != null) {
            List<WebElement> boxes = DriverUtils.getWebDriver().findElements(By.xpath(sectionXPath + optionXPath + "//span[contains(@class,'box')]"));
            if (!boxes.isEmpty()) boxes.get(0).click(); else option.click();
        }
    }

    @Step("Enter price range from {minPrice} to {maxPrice}")
    public void enterPriceRange(double minPrice, double maxPrice) {
        priceRangeMinInput.setText(formatPrice(minPrice));
        priceRangeMaxInput.setText(formatPrice(maxPrice));
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
        return filterDialogTitle.isVisible();
    }

    @Step("Click filter action: {actionText}")
    public void clickFilterAction(String actionText) {
        WebElement actionsContainer = DriverUtils.getWebDriver().findElement(By.xpath(actionsContainerXPath));
        String actionXp = String.format(dynamicActionText, actionText);
        WebElement action = actionsContainer.findElement(By.xpath(actionXp));
        action.click();
    }

    @Step("Get CSS color of highlighted pill '{optionText}' under section '{sectionLabel}'")
    public String getHighlightedPillColor(String sectionLabel, String optionText) {
        String containerXp = String.format(sectionPillContainerByTitle, sectionLabel);
        String pillXp = String.format(pillButtonByTextActive, optionText);
        List<WebElement> pills = DriverUtils.getWebDriver().findElements(By.xpath(containerXp + pillXp));
        if (pills.isEmpty()) {
            return "";
        }
        WebElement pillButton = pills.get(0);
        WebElement innerDiv = findFirst(pillButton, ".//div");
        String color = innerDiv != null ? innerDiv.getCssValue("color") : pillButton.getCssValue("color");
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

    @Step("Verify checkbox state for option '{optionText}' under section '{sectionLabel}' is checked")
    public boolean isCheckboxOptionChecked(String sectionLabel, String optionText) {
        String sectionXPath = String.format(dynamicSectionLabel, sectionLabel);
        List<WebElement> secs = DriverUtils.getWebDriver().findElements(By.xpath(sectionXPath));
        if (secs.isEmpty()) return false;
        String optionXPath = String.format(dynamicOptionText, optionText, optionText);
        WebElement option = findFirst(secs.get(0), optionXPath);
        if (option == null) return false;
        boolean classChecked = !option.findElements(By.xpath(optionCheckboxCheckedRel)).isEmpty();
        boolean iconOn = !option.findElements(By.xpath(optionIconCheckOnRel)).isEmpty();
        return classChecked || iconOn;
    }

    @Step("Quick filter pill active anywhere: {text}")
    public boolean isQuickFilterPillActive(String text) {
        String xp1 = String.format(activePillAnywhereByText, text);
        String xp2 = String.format(activePillAnywhereByInnerDiv, text);
        boolean exists = !DriverUtils.getWebDriver().findElements(By.xpath(xp1)).isEmpty() || !DriverUtils.getWebDriver().findElements(By.xpath(xp2)).isEmpty();
        return exists;
    }

    @Step("Inline filter-child2 checked: {text}")
    public boolean isInlineFilterChild2Checked(String text) {
        String xpExact = String.format(inlineFilterChild2CheckedExact, text);
        if (!DriverUtils.getWebDriver().findElements(By.xpath(xpExact)).isEmpty()) return true;
        String xpContains = String.format(inlineFilterChild2CheckedContains, text);
        return !DriverUtils.getWebDriver().findElements(By.xpath(xpContains)).isEmpty();
    }

    @Step("Check option highlighted/selected in section: '{sectionLabel}' option: '{optionText}'")
    public boolean isSupplierHighlighted(String sectionLabel, String optionText) {
        boolean pillBlue = isHighlightedPillBlue(sectionLabel, optionText);
        boolean checkboxChecked = isCheckboxOptionChecked(sectionLabel, optionText);
        boolean quickPill = isQuickFilterPillActive(optionText);
        boolean child2Checked = isInlineFilterChild2Checked(optionText);
        if (pillBlue || checkboxChecked || quickPill || child2Checked) {
            return true;
        }
        Allure.step(String.format("Highlight NOT detected for section='%s', option='%s' (pillBlue=%s, checkboxChecked=%s, quickPill=%s, child2=%s)",
                sectionLabel, optionText, pillBlue, checkboxChecked, quickPill, child2Checked));
        return false;
    }

    @Step("Verify current URL matches category navigation: {category}")
    public boolean isLinkActiveCategoryNavigation(Category category) {
        String href = category.getHref();
        try {
            com.codeborne.selenide.Selenide.webdriver().shouldHave(WebDriverConditions.urlContaining(href), Duration.ofSeconds(20));
        } catch (Throwable ignored) {

        }
        String current = getCurrentUrl();
        return current != null && current.contains(href);
    }

    @Step("Open left menu main category: {title}")
    public void openLeftMenuMainCategory(String title) {
        String xp = String.format(leftMenuMainCategoryAnchorByTitle, title);
        DriverUtils.getWebDriver().findElement(By.xpath(xp)).click();
    }

    @Step("Verify left menu section displayed: {title}")
    public boolean isLeftMenuSectionDisplayed(String title) {
        String xp = String.format(leftMenuMainCategoryTitleByText, title);
        return !DriverUtils.getWebDriver().findElements(By.xpath(xp)).isEmpty();
    }

    @Step("Count left menu sub items for current section")
    public int countLeftMenuSubItems() {
        WebElement container = DriverUtils.getWebDriver().findElement(By.xpath(leftMenuSubItemsContainer));
        return container.findElements(By.xpath(leftMenuSubItemRel)).size();
    }

    @Step("Click 'Xem thêm' in left menu if present")
    public boolean clickLeftMenuSeeMore() {
        List<WebElement> more = DriverUtils.getWebDriver().findElements(By.xpath(leftMenuTogglerMore));
        if (!more.isEmpty() && more.get(0).isDisplayed()) {
            more.get(0).click();
            return true;
        }
        return false;
    }

    @Step("Count visible products in grid")
    public int countVisibleProducts() {
        return productCards.getElements().size();
    }

    private WebElement findFirst(WebElement scope, String relXpath) {
        try {
            List<WebElement> list = scope.findElements(By.xpath(relXpath));
            return list.isEmpty() ? null : list.get(0);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean elementExists(WebElement scope, String relXpath) {
        try {
            return !scope.findElements(By.xpath(relXpath)).isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean existsRel(WebElement scope, String relXpath) {
        return elementExists(scope, relXpath);
    }

    private String findIfExistsText(WebElement scope, String relXpath) {
        try {
            List<WebElement> found = scope.findElements(By.xpath(relXpath));
            return found.isEmpty() ? "" : found.get(0).getText();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private String findIfExistsAttr(WebElement scope, String relXpath, String attr) {
        try {
            List<WebElement> found = scope.findElements(By.xpath(relXpath));
            return found.isEmpty() ? null : found.get(0).getAttribute(attr);
        } catch (Throwable ignored) {
            return null;
        }
    }
}