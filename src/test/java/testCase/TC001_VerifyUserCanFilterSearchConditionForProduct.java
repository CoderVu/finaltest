package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import messages.AssertMessages;
import org.example.report.SoftAssertConfig;
import org.example.enums.BreadcrumbToCategory;
import org.example.enums.Category;
import org.example.enums.ViewType;
import org.example.pages.ShopPage;
import org.example.pages.HomePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TC001_VerifyUserCanFilterSearchConditionForProduct extends TestBase {

    HomePage homePage = new HomePage();
    ShopPage shopPage = new ShopPage();
    SoftAssertConfig softAssert =  SoftAssertConfig.get();

    // Test data
    private final Category topCategory = Category.DIEN_GIA_DUNG;
    private final BreadcrumbToCategory breadcrumbPath = BreadcrumbToCategory.HOME_TO_DIEN_GIA_DUNG;
    private final String leftMenuCategoryName = "Máy pha cà phê";
    private final String h2CategoryTitle = "Điện Gia Dụng";
    private final String brand = "DeLonghi";
    private final String color = "Đen";
    private final String rating = "từ 4 sao";
    private final String sectionBrand = "Thương hiệu";
    private final String sectionColor = "Màu sắc";
    private final String sectionRating = "Đánh giá";
    private final int minPrice = 500000;
    private final int maxPrice = 5500000;
    private final ViewType viewType = ViewType.GRID;

    @BeforeMethod
    public void setUp() {
        super.setUp();
    }

    @Test(description = "Navigate to TIKI website and apply coffee machine filters")
    @Epic("Tiki E-commerce")
    @Story("Verify user can filter search condition for product")
    @Severity(SeverityLevel.NORMAL)
    public void testNavigateToHomePageAndApplyCoffeeMachineFilters() {

        // Step 1: Navigate to TIKI website
        homePage.navigateToHomePage();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), AssertMessages.HOME_NOT_DISPLAYED);

        // Step 2: Click on "Tìm kiếm" field in site
        homePage.clickSearchField();

        // Step 3: Click on "Điện Gia Dụng" in "Danh Mục Nổi Bật" section (search dropdown)
        homePage.selectSearchTopCategory(topCategory);
        softAssert.assertTrue(
                shopPage.isLinkActiveCategoryNavigation(topCategory),
                String.format(AssertMessages.URL_SHOULD_NAVIGATE, topCategory.getHref())
        );
        softAssert.assertTrue(
                shopPage.verifyBreadcrumbToCategory(breadcrumbPath),
                String.format(AssertMessages.BREADCRUMB_SHOULD_BE, breadcrumbPath.getFullBreadcrumb())
        );
        softAssert.assertTrue(
                shopPage.isH2TitleDisplayed(h2CategoryTitle),
                String.format(AssertMessages.H2_TITLE_NOT_DISPLAYED, h2CategoryTitle)
        );

        // Step 4: Select left menu: "Máy pha cà phê"
        shopPage.selectLeftMenuCategory(leftMenuCategoryName);

        // Step 5: Select filter options in "Tất cả sản phẩm" section
        shopPage.clickAllFiltersButton();
        softAssert.assertTrue(
                shopPage.isFilterDialogTitleDisplayed(),
                AssertMessages.FILTER_DIALOG_NOT_DISPLAYED
        );
        
        // Select Thương hiệu: "DeLonghi"
        shopPage.selectFilterOption(sectionBrand, brand);
        
        // Select Màu sắc: "Đen"
        shopPage.selectFilterOption(sectionColor, color);
        
        // Select Đánh giá: "từ 4 sao"
        shopPage.selectFilterOption(sectionRating, rating);
        softAssert.assertTrue(
                shopPage.isModalOptionChecked(sectionBrand, brand),
                String.format(AssertMessages.FILTER_CHECKED_IN_SECTION, brand, sectionBrand)
        );
        softAssert.assertTrue(
                shopPage.isModalOptionChecked(sectionRating, rating),
                String.format(AssertMessages.FILTER_CHECKED_IN_SECTION, rating, sectionRating)
        );
        softAssert.assertTrue(
                shopPage.isModalOptionChecked(sectionColor, color),
                String.format(AssertMessages.FILTER_CHECKED_IN_SECTION, color, sectionColor)
        );

        // Step 6: Enter price range, then select Xem Kết quả button 500.000 - 5.500.000
        shopPage.applyFiltersWithPriceRange(minPrice, maxPrice);

        // "Thương hiệu" are "DELONGHI" and "price" of all displayed items in the result grid is within 500.000 - 5.500.000
        softAssert.assertTrue(
                shopPage.isSupplierHighlighted(sectionBrand, brand),
                String.format(AssertMessages.FILTER_CHECKED_IN_SECTION, brand, sectionBrand)
        );
        softAssert.assertTrue(
                shopPage.isSupplierHighlighted(sectionColor, color),
                String.format(AssertMessages.FILTER_CHECKED_IN_SECTION, color, sectionColor)
        );
        softAssert.assertTrue(
                shopPage.checkProductView(viewType),
                AssertMessages.PRODUCT_VIEW_NOT_MATCH
        );
        softAssert.assertFalse(
                shopPage.areAllProductModelPricesWithin(minPrice, maxPrice),
                String.format(AssertMessages.PRICE_RANGE_NOT_MATCH, (double) minPrice, (double) maxPrice)
        );

        softAssert.assertAll();
    }

}
