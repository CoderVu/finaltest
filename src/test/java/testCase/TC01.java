package testCase;

import config.TestBase;
import org.example.config.SoftAssertConfig;
import org.example.enums.Category;
import org.example.pages.HomePage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TC01 extends TestBase {

    HomePage homePage = new HomePage();
    SoftAssertConfig softAssert =  SoftAssertConfig.get();

    @BeforeMethod
    public void setUp() {
        super.setUp();



    }

    @Test(description = "Navigate to Home Page and verify title")
    public void testNavigateToHomePage() {
        // Test steps would go here

        homePage.navigateToHomePage();
        softAssert.assertFalse(homePage.isHomePageDisplayed(), "Home page is not displayed");

        homePage.selectCategory(Category.BALO_VA_VALI);

        softAssert.assertTrue(false, "This is a sample failure for demonstration");





        SoftAssertConfig.assertAllSoft();
    }

}
