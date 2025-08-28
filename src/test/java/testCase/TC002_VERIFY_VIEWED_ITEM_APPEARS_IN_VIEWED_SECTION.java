package testCase;

import config.TestBase;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.config.SoftAssertConfig;
import org.example.enums.Category;
import org.example.pages.BasePage;
import org.example.pages.HomePage;
import org.example.pages.ShopPage;
import org.example.models.Product;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC002_VERIFY_VIEWED_ITEM_APPEARS_IN_VIEWED_SECTION extends TestBase {

    HomePage homePage = new HomePage();
    ShopPage shopPage = new ShopPage();
    SoftAssertConfig softAssert = SoftAssertConfig.get();

    @BeforeMethod
    public void setUp() {
        super.setUp();
    }

    @Test(description = "Verify viewed item displays in 'Sản phẩm bạn đã xem' section")
    @Epic("Tiki E-commerce")
    @Story("Viewed items appear under history section")
    @Severity(SeverityLevel.NORMAL)
    public void testViewedItemAppearsInHistory() {

        homePage.navigateToHomePage();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed");

        homePage.selectCategory(Category.THE_THAO_DA_NGOAI);

        Product viewed = shopPage.selectAnyProductAndOpenDetails();

        homePage.backToHome();
        softAssert.assertTrue(homePage.isHomePageDisplayed(), "Home page is not displayed after back");
     
        homePage.selectCategory(Category.NHA_SACH_TIKI);

        softAssert.assertTrue(shopPage.checkViewHistoryProduct(viewed),
                "Viewed item (name, price, image) should appear in 'Sản phẩm bạn đã xem' section"
        );

        softAssert.assertAll();
    }
}
