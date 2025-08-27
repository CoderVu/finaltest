package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.config.SoftAssertConfig;
import org.example.enums.BreadcrumbToCategory;
import org.example.enums.Category;
import org.example.pages.ShopPage;
import org.example.pages.HomePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TC001_VERIFY_USER_CAN_FILTER_SEARCH_CONDITION_FOR_PRODUCT extends TestBase {

    HomePage homePage = new HomePage();
    ShopPage shopPage = new ShopPage();
    SoftAssertConfig softAssert =  SoftAssertConfig.get();

    @BeforeMethod
    public void setUp() {
        super.setUp();
    }

    @Test(description = "Navigate to Home Page and verify title")
    @Epic("Tiki E-commerce")
    @Story("User navigates to book category and applies filters")
    @Severity(SeverityLevel.NORMAL)
    public void testNavigateToHomePage() {

        homePage.navigateToHomePage();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed");

        homePage.selectCategory(Category.NHA_SACH_TIKI);
        softAssert.assertTrue(shopPage.verifyBreadcrumbToCategory(BreadcrumbToCategory.HOME_TO_NHA_SACH_TIKI),
                "Breadcrumb should be '" + BreadcrumbToCategory.HOME_TO_NHA_SACH_TIKI.getFullBreadcrumb() + "'");

        shopPage.clickAllFiltersButton();
        softAssert.assertTrue(shopPage.isFilterDialogTitleDisplayed(), "'Bộ lọc' dialog title should be displayed");
        shopPage.selectFilterOption("Nhà cung cấp", "Nhà sách Fahasa");

        shopPage.applyFiltersWithPriceRange(60000, 140000);

        softAssert.assertTrue(shopPage.isSupplierHighlighted("Nhà cung cấp", "Nhà sách Fahasa"),
                "Supplier 'Nhà sách Fahasa' should be highlighted");
        softAssert.assertTrue(shopPage.isGridView(), "Product listing should be in Grid View mode");
        softAssert.assertTrue(shopPage.areAllProductModelPricesWithin(60000, 140000),
                "All displayed prices should be within 60.000đ - 140.000đ");

        softAssert.assertAll();
    }

}
