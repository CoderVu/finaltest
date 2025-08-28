package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.config.SoftAssertConfig;
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
        softAssert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed");

        // Step 2: Click on "Tìm kiếm" field in site
        homePage.clickSearchField();

        // Step 3: Click on "Điện Gia Dụng" in "Danh Mục Nổi Bật" section (search dropdown)
        homePage.selectSearchTopCategory(Category.DIEN_GIA_DUNG);
        softAssert.assertTrue(shopPage.isLinkActiveCategoryNavigation(Category.DIEN_GIA_DUNG),
                "Should navigate to '" + Category.DIEN_GIA_DUNG.getHref() + "'");
        softAssert.assertTrue(shopPage.verifyBreadcrumbToCategory(BreadcrumbToCategory.HOME_TO_DIEN_GIA_DUNG),
                "Breadcrumb should be '" + BreadcrumbToCategory.HOME_TO_DIEN_GIA_DUNG.getFullBreadcrumb() + "'");

        // Step 4: Select left menu: "Máy pha cà phê"
        shopPage.selectLeftMenuCategory("Máy pha cà phê");

        // Step 5: Select filter options in "Tất cả sản phẩm" section
        shopPage.clickAllFiltersButton();
        softAssert.assertTrue(shopPage.isFilterDialogTitleDisplayed(), "'Bộ lọc' dialog title should be displayed");
        
        // Select Thương hiệu: "DeLonghi"
        shopPage.selectFilterOption("Thương hiệu", "DeLonghi");
        
        // Select Màu sắc: "Đen"
        shopPage.selectFilterOption("Màu sắc", "Đen");
        
        // Select Đánh giá: "từ 4 sao"
        shopPage.selectFilterOption("Đánh giá", "từ 4 sao");
        softAssert.assertTrue(shopPage.isModalOptionChecked("Thương hiệu", "DeLonghi"));
        softAssert.assertTrue(shopPage.isModalOptionChecked("Đánh giá", "từ 4 sao"));
        softAssert.assertTrue(shopPage.isModalOptionChecked("Màu sắc", "Đen"));

        // Step 6: Enter price range, then select Xem Kết quả button 500.000 - 5.000.000
        shopPage.applyFiltersWithPriceRange(500000, 5000000);

        // Verify that: "Thương hiệu" are "DELONGHI" and "price" of all displayed items in the result grid is within 500.000 - 5.000.000
        softAssert.assertTrue(shopPage.isSupplierHighlighted("Thương hiệu", "DeLonghi"), "Supplier 'DeLonghi' should be highlighted");
        softAssert.assertTrue(shopPage.isSupplierHighlighted("Màu sắc", "Đen"), "Color filter 'Đen' should be highlighted");
        softAssert.assertTrue(shopPage.checkProductView(ViewType.GRID), "Product listing should be in Grid View mode");
        softAssert.assertTrue(shopPage.areAllProductModelPricesWithin(500000, 5500000),
                "All displayed prices should be within 500.000đ - 5.500.000đ range");

        softAssert.assertAll();
    }

}
