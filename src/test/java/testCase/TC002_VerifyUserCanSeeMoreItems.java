package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.config.SoftAssertConfig;
import org.example.enums.Category;
import org.example.pages.HomePage;
import org.example.pages.ShopPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC002_VerifyUserCanSeeMoreItems extends TestBase {

    HomePage homePage = new HomePage();
    ShopPage shopPage = new ShopPage();
    SoftAssertConfig softAssert = SoftAssertConfig.get();

    @BeforeMethod
    public void setUp() {
        super.setUp();
    }

    @Test(description = "Verify 'Xem thêm' loads more products in Tivi category")
    @Epic("Tiki E-commerce")
    @Story("'Xem thêm' button loads more products")
    @Severity(SeverityLevel.NORMAL)
    public void testSeeMoreLoadsMoreProducts() {

        // 1. Navigate to TIKI website
        homePage.navigateToHomePage();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed");

        // 2. Select left menu Điện Tử - Điện Lạnh and verify URL
        homePage.selectCategory(Category.DIEN_TU_DIEN_LANH);
        softAssert.assertTrue(shopPage.isLinkActiveCategoryNavigation(Category.DIEN_TU_DIEN_LANH),
                "Should navigate to '" + Category.DIEN_TU_DIEN_LANH.getHref() + "'");

        // 3. Click Danh Mục "Tivi" and verify section / navigation
        shopPage.openLeftMenuMainCategory("Tivi");
        softAssert.assertTrue(shopPage.isLeftMenuSectionDisplayed("Tivi"), "'Tivi' section should be displayed");

        // 4. Click on Xem thêm button to see more items
        shopPage.waitForSizeProductGreaterThan(0);
        int before = shopPage.countVisibleProducts();
        shopPage.loadMoreProducts(2);
        int after = shopPage.countVisibleProducts();
        softAssert.assertTrue(after > before, "Product count should increase after 'Xem thêm'. before=" + before + ", after=" + after);

        softAssert.assertAll();
    }
}
