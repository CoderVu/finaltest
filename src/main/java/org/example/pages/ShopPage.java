package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.CollectionCondition;

import java.time.Duration;
import java.util.*;
import java.math.BigDecimal;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverConditions;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.example.enums.BreadcrumbToCategory;
import org.example.models.Product;
import org.example.enums.ViewType;

import static com.codeborne.selenide.Selenide.*;
import static org.example.utils.FormatUtils.formatPrice;

import org.example.enums.Category;

import static org.example.utils.WebDriverUtils.getCurrentUrl;

@Slf4j
public class ShopPage extends BasePage {

    // Locators
    protected static final SelenideElement breadcrumb = $x("//div[contains(@class,'breadcrumb')]");
    protected final SelenideElement allProductsSection = $(".sc-dc63c727-1");
    protected final SelenideElement filterAllButton = $x("//button[.//div[normalize-space(text())='Tất cả']]");
    protected final SelenideElement filterDialogTitle = $(".title");

    protected final SelenideElement priceRangeMinInput = $("input[placeholder='Từ']");
    protected final SelenideElement priceRangeMaxInput = $("input[placeholder='Đến']");
    protected final SelenideElement viewResultButton = $("#view-result");
    protected final ElementsCollection productCards = $$x("//a[contains(@class,'product-ditem')]");
    protected final ElementsCollection productPrices = $$x("//a[contains(@class,'product-sitem')]//*[self::div or self::span][contains(@class,'price-discount__price') or contains(@class,'price-current')][1]");
    protected final SelenideElement loadMoreButton = $x("//div[@data-view-id='category_infinity_view.more']");
    protected final SelenideElement selectedSupplierLabel = $("#selected-supplier");
    protected final ElementsCollection actualItemsXpath = $$x(".//a[contains(@class,'breadcrumb-item')]//span");
    protected static final SelenideElement filled = $x(".//div[@style and contains(@style,'width')]");
    protected static final String h2ByExactText = "//h2[normalize-space(text())='%s']";
    // Rel/absolute xpaths as constants
    protected static final String togglerRel = ".//a[contains(@class,'toggler')]";
    protected static final String modalTogglerMoreRel = ".//a[contains(@class,'toggler') and normalize-space(text())='Xem thêm']";
    protected static final String actionsContainerXPath = "//div[contains(@class,'sc-add2a4bc-5')]";

    // Dynamic locators
    protected static final String dynamicSectionLabel = "//div[@data-view-label='%s']";
    protected static final String dynamicOptionText = ".//*[self::label or self::div][(contains(@class,'item') or contains(@class,'filter-child2'))][.//span[normalize-space(text())='%s'] or .//div[normalize-space(text())='%s']]";
    protected static final String dynamicActionText = ".//div[normalize-space(text())='%s']";
    protected static final String dynamicLeftMenuCategory = "//div[contains(@class,'sc-36d678cb-6')]//a[contains(text(),'%s')]";

    // Pill highlight locators (top-defined, to be called below)
    protected static final String sectionPillContainerByTitle = "//div[@data-view-label='%s']";
    protected static final String pillButtonByTextActive = ".//button[contains(@class,'filter-child') and .//div[normalize-space(text())='%s'] and contains(@class,'izfbuI')]";
    protected static final String pillButtonActiveText = ".//button[contains(@class,'filter-child') and contains(@class,'izfbuI')]//div";
    protected static final String activePillAnywhereByText = "//button[contains(@class,'filter-child') and contains(@class,'izfbuI') and .//div[normalize-space(text())='%s']]";
    protected static final String activePillAnywhereByInnerDiv = "//div[contains(@class,'sc-bd134f7-2') and normalize-space(text())='%s']";
    protected static final String inlineFilterChild2CheckedExact = "//div[contains(@class,'filter-child2')][.//span[contains(@class,'box') and contains(@class,'checked')]]//*[self::span or self::div][normalize-space(text())='%s']";
    protected static final String inlineFilterChild2CheckedContains = "//div[contains(@class,'filter-child2')][.//span[contains(@class,'box') and contains(@class,'checked')]]//*[self::span or self::div][contains(normalize-space(.), '%s')]";
    protected static final String optionCheckboxCheckedRel = ".//span[contains(@class,'box') and contains(@class,'checked')]";
    protected static final String optionIconCheckOnRel = ".//img[contains(@class,'icon-check-on')]";

    // Product card relative locators
    protected static final String productCardNameRel = ".//h3";
    protected static final String productCardPriceRel = ".//*[contains(@class,'price-discount__price') or contains(@class,'price-current')][1]";
    protected static final String productCardImageRel = ".//img";
    protected static final String productCardLinkRel = ".//a[contains(@class,'product-item')]|self::a[contains(@class,'product-item')]";
    protected static final String productCardRatingWrapperRel = ".//div[contains(@class,'sc-980e9960-0')]";
    protected static final String productCardsXpath = "//a[contains(@class,'product-item')]";
    // Viewed history section locators
    protected static final String viewedHistorySectionTitleXp = "//h2[normalize-space(text())='Sản phẩm đã xem']";
    protected static final String viewedHistorySectionRootFromTitleXp = "//div[@data-view-id='product_list_recently_view_container']";
    protected static final String viewedHistoryItemRel = ".//a[@data-view-id='product_list_recently_view_item']";
    protected static final String viewedHistoryNameRel = ".//h3";
    protected static final String viewedHistoryPriceRel = ".//p[contains(@class,'price')]//span[1]";
    protected static final String viewedHistoryImageRel = ".//img";

    // Modal (All filters) locators
    protected static final String allFiltersModalRoot = "//div[contains(@class,'sc-add2a4bc-2')]";
    protected static final String modalSectionByTitle = ".//div[@data-view-id='search_filter_container' or @data-view-id='search_checkbox_filter_container'][.//h4[@class='title' and normalize-space(text())='%s']]";
    protected static final String modalItemByText = ".//*[self::label or self::div][(contains(@class,'item') or contains(@class,'filter-child2'))][.//span[normalize-space(text())='%s'] or .//div[normalize-space(text())='%s']]";
    protected static final String modalCheckedBoxRel = ".//span[contains(@class,'box') and contains(@class,'checked')]";

    // Left menu (category panel) locators
    protected static final String leftMenuRoot = "//div[contains(@class,'sc-36d678cb-1')]";
    protected static final String leftMenuMainCategoryAnchorByTitle = leftMenuRoot + "//a[.//div[contains(@class,'sc-36d678cb-3') and normalize-space(text())='%s']]";
    protected static final String leftMenuMainCategoryTitleByText = leftMenuRoot + "//div[contains(@class,'sc-36d678cb-3') and normalize-space(text())='%s']";
    protected static final String leftMenuSubItemsContainer = leftMenuRoot + "//div[contains(@class,'sc-36d678cb-5')]";
    protected static final String leftMenuSubItemRel = ".//div[contains(@class,'sc-36d678cb-6')]";
    protected static final String leftMenuTogglerMore = leftMenuRoot + togglerRel;

    @Step("Verify modal selection: section='{sectionTitle}', option='{optionText}' is checked")
    public boolean isModalOptionChecked(String sectionTitle, String optionText) {
        SelenideElement modal = $x(allFiltersModalRoot).shouldBe(Condition.visible, Duration.ofSeconds(10));
        SelenideElement section = modal.$x(String.format(modalSectionByTitle, sectionTitle)).shouldBe(Condition.visible, Duration.ofSeconds(10));
        SelenideElement item = section.$x(String.format(modalItemByText, optionText, optionText));
        if (!item.exists()) {
            SelenideElement more = section.$x(modalTogglerMoreRel);
            if (more.exists()) more.click();
            item = section.$x(String.format(modalItemByText, optionText, optionText));
        }
        if (!item.exists()) return false;
        boolean checked = item.$x(modalCheckedBoxRel).exists();
        if (!checked) {
            try {
                item.scrollTo();
            } catch (Throwable ignored) {
            }
            checked = item.$x(modalCheckedBoxRel).exists();
        }
        return checked;
    }

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

    @Step("Verify H2 page title displayed: {title}")
    public boolean isH2TitleDisplayed(String title) {
        String xp = String.format(h2ByExactText, title);
        return $x(xp).exists();
    }

    @Step("Click on 'Tất cả' button under 'Tất cả sản phẩm' section")
    public void clickAllFiltersButton() {
        allProductsSection.shouldBe(Condition.visible);
        filterAllButton.shouldBe(Condition.visible).click();
    }

    @Step("Select left menu category: {categoryName}")
    public void selectLeftMenuCategory(String categoryName) {
        String leftMenuXPath = String.format(dynamicLeftMenuCategory, categoryName);
        $x(leftMenuXPath).shouldBe(Condition.visible).click();
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
            } catch (Exception ignored) {
            }

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
    public void waitForSizeProductGreaterThan(Integer moreThanQuantity) {
        productCards.shouldHave(CollectionCondition.sizeGreaterThan(moreThanQuantity), Duration.ofSeconds(10));
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

    @Step("Click 'Xem thêm' until all products are loaded or no more growth")
    public void loadMoreProductsAll() {
        int safety = 30;
        int previousCount = -1;
        while (safety-- > 0) {
            if (!isLoadMoreVisible()) break;
            int before = productCards.size();
            try {
                loadMoreButton.scrollTo().shouldBe(Condition.interactable).click();
            } catch (Throwable ignored) {
                break;
            }
            waitForSizeProductGreaterThan(before);
            int after = productCards.size();
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
                size = productCards.size();
                if (size > 0) {
                    href = productCards.first().getAttribute("href");
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
                ElementsCollection cards = $$x(productCardsXpath);
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
                        // force retry on staleness
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
        } catch (Exception ignored) {
        }

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
        } catch (Exception ignored) {
        }
        return null;
    }

    @Step("Select filter option: section='{sectionLabel}', option='{optionText}'")
    public void selectFilterOption(String sectionLabel, String optionText) {
        String sectionXPath = String.format(dynamicSectionLabel, sectionLabel);
        SelenideElement section = $x(sectionXPath).scrollTo().shouldBe(Condition.visible, Duration.ofSeconds(10));
        String optionXPath = String.format(dynamicOptionText, optionText, optionText);
        SelenideElement option = section.$x(optionXPath);
        if (!option.exists()) {
            SelenideElement toggleEl = section.$x(togglerRel);
            if (toggleEl.exists()) {
                String toggleText = toggleEl.getText().trim();
                if (toggleText.equalsIgnoreCase("Xem thêm")) {
                    toggleEl.click();
                }
            }
            option = section.$x(optionXPath);
        }
        option.shouldBe(Condition.visible, Duration.ofSeconds(10));
        SelenideElement checkboxBox = option.$x(".//span[contains(@class,'box')]");
        (checkboxBox.exists() ? checkboxBox : option).click();
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
        SelenideElement pillButton = $x(container).$x(pill);
        if (!pillButton.exists()) {
            return "";
        }
        pillButton.shouldBe(Condition.visible);
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

    @Step("Verify checkbox state for option '{optionText}' under section '{sectionLabel}' is checked")
    public boolean isCheckboxOptionChecked(String sectionLabel, String optionText) {
        String sectionXPath = String.format(dynamicSectionLabel, sectionLabel);
        SelenideElement section = $x(sectionXPath);
        if (!section.exists()) return false;
        String optionXPath = String.format(dynamicOptionText, optionText, optionText);
        SelenideElement option = section.$x(optionXPath);
        if (!option.exists()) return false;

        boolean classChecked = option.$x(optionCheckboxCheckedRel).exists();
        boolean iconOn = option.$x(optionIconCheckOnRel).exists();
        return classChecked || iconOn;
    }

    @Step("Quick filter pill active anywhere: {text}")
    public boolean isQuickFilterPillActive(String text) {
        String xp1 = String.format(activePillAnywhereByText, text);
        String xp2 = String.format(activePillAnywhereByInnerDiv, text);
        boolean exists = $x(xp1).exists() || $x(xp2).exists();
        if (exists) {
            try {
                ($x(xp1).exists() ? $x(xp1) : $x(xp2)).scrollTo();
            } catch (Throwable ignored) {
            }
        }
        return exists;
    }

    @Step("Inline filter-child2 checked: {text}")
    public boolean isInlineFilterChild2Checked(String text) {
        String xpExact = String.format(inlineFilterChild2CheckedExact, text);
        if ($x(xpExact).exists()) return true;
        String xpContains = String.format(inlineFilterChild2CheckedContains, text);
        return $x(xpContains).exists();
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
            webdriver().shouldHave(WebDriverConditions.urlContaining(href), Duration.ofSeconds(20));
        } catch (Throwable ignored) {

        }
        String current = getCurrentUrl();
        return current != null && current.contains(href);
    }

    @Step("Open left menu main category: {title}")
    public void openLeftMenuMainCategory(String title) {
        String xp = String.format(leftMenuMainCategoryAnchorByTitle, title);
        SelenideElement anchor = $x(xp).shouldBe(Condition.visible, Duration.ofSeconds(10));
        anchor.scrollTo().click();
    }

    @Step("Verify left menu section displayed: {title}")
    public boolean isLeftMenuSectionDisplayed(String title) {
        String xp = String.format(leftMenuMainCategoryTitleByText, title);
        return $x(xp).exists();
    }

    @Step("Count left menu sub items for current section")
    public int countLeftMenuSubItems() {
        SelenideElement container = $x(leftMenuSubItemsContainer).shouldBe(Condition.exist, Duration.ofSeconds(10));
        return container.$$x(leftMenuSubItemRel).size();
    }

    @Step("Click 'Xem thêm' in left menu if present")
    public boolean clickLeftMenuSeeMore() {
        SelenideElement more = $x(leftMenuTogglerMore);
        if (more.exists() && more.is(Condition.visible)) {
            more.scrollTo().click();
            return true;
        }
        return false;
    }

    @Step("Count visible products in grid")
    public int countVisibleProducts() {
        return productCards.size();
    }
}