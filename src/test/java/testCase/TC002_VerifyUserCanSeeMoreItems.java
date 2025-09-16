package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import messages.AssertMessages;
import org.example.report.SoftAssertConfig;
import org.example.enums.Category;
import org.example.pages.HomePage;
import org.example.pages.ShopPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC002_VerifyUserCanSeeMoreItems extends TestBase {

    HomePage homePage = new HomePage();
    ShopPage shopPage = new ShopPage();
    SoftAssertConfig softAssert;

    // Test data
    private final Category mainCategory = Category.DIEN_TU_DIEN_LANH;
    private final String mainCategoryName = "Tivi";
    private final int loadMoreClicks = 2;

    @BeforeMethod
    public void setUp() {
        super.setUp();
        softAssert = SoftAssertConfig.get();
    }

    @Test(description = "Verify 'Xem thêm' loads more products in Tivi category")
    @Epic("Tiki E-commerce")
    @Story("'Xem thêm' button loads more products")
    @Severity(SeverityLevel.NORMAL)
    public void testSeeMoreLoadsMoreProducts() {

        // 1. Navigate to TIKI website
        homePage.navigateToHomePage();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), AssertMessages.HOME_NOT_DISPLAYED);

        // 2. Select left menu Điện Tử - Điện Lạnh and verify URL
        homePage.selectCategory(mainCategory);
        softAssert.assertTrue(
                shopPage.isLinkActiveCategoryNavigation(mainCategory),
                String.format(AssertMessages.URL_SHOULD_NAVIGATE, mainCategory.getHref())
        );

        // 3. Click Danh Mục "Tivi" and verify section / navigation
        shopPage.openLeftMenuMainCategory(mainCategoryName);
        softAssert.assertTrue(
                shopPage.isLeftMenuSectionDisplayed(mainCategoryName),
                String.format(AssertMessages.SECTION_SHOULD_BE_DISPLAYED, mainCategoryName)
        );

        // 4. Click on Xem thêm button to see more items
        shopPage.waitForSizeProductGreaterThan(0);
        int before = shopPage.countVisibleProducts();
        shopPage.loadMoreProducts(loadMoreClicks);
        int after = shopPage.countVisibleProducts();
        softAssert.assertTrue(
                after > before,
                String.format(AssertMessages.PRODUCT_COUNT_INCREASED, before, after)
        );

        softAssert.assertAll();
    }
}
