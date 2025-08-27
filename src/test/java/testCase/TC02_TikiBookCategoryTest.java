package testCase;

import org.example.config.SoftAssertConfig;
import org.example.enums.Breadcrumb;
import org.example.enums.Category;
import org.example.pages.BookCategoryPage;
import org.example.pages.HomePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

public class TC02_TikiBookCategoryTest extends TestBase {

     HomePage homePage = new HomePage();
    SoftAssertConfig softAssert =  SoftAssertConfig.get();

    @BeforeMethod
    public void setUp() {
        super.setUp();



    }
    @Test(description = "Test comprehensive Tiki book category workflow")
    @Epic("Tiki E-commerce")
    @Feature("Book Category Navigation")
    @Story("User navigates to book category and applies filters")
    @Severity(SeverityLevel.CRITICAL)
    public void testTikiBookCategoryWorkflow() {
        
        // Step 1: Navigate to Tiki website
        homePage.navigateToHomePage();
        
        // Step 2: Select left menu "Nhà Sách Tiki"
        BookCategoryPage bookCategoryPage = homePage.selectCategory(Category.NHA_SACH_TIKI);
        // Verify breadcrumb using enum parameter
        softAssert.assertTrue(bookCategoryPage.verifyBreadcrumb(Breadcrumb.HOME_TO_NHA_SACH_TIKI), 
                  "Breadcrumb should be '" + Breadcrumb.HOME_TO_NHA_SACH_TIKI.getFullBreadcrumb() + "'");
        
        // Step 4: Click "Tất cả" button
        bookCategoryPage.clickAllFiltersButton();
        
        // Step 5: Verify filter dialog is displayed
        softAssert.assertTrue(bookCategoryPage.verifyFilterDialogDisplayed(), "Filter dialog should be displayed");
        
        // Step 6: Check supplier 'Nhà sách Fahasa'
        bookCategoryPage.selectSupplierFahasa();
        
        // Step 7: Enter price range 60.000-140.000
        bookCategoryPage.enterPriceRange(60000, 140000);
        
        // Step 8: Apply filters
        bookCategoryPage.applyFilters();
        
        // Step 9: Verify supplier is highlighted (selected)
        softAssert.assertTrue(bookCategoryPage.verifySupplierSelected(), "Supplier 'Nhà sách Fahasa' should be selected/highlighted");
        
        // Step 10: Verify that product prices are within the specified range
        softAssert.assertTrue(bookCategoryPage.verifyProductPricesInRange(60000, 140000), 
                  "All product prices should be within range 60.000đ - 140.000đ");

        SoftAssertConfig.assertAllSoft();
    }
}
