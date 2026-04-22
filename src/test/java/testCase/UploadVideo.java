package testCase;

import config.TestBase;
import lombok.extern.slf4j.Slf4j;
import org.example.core.assertion.Assert;
import org.testng.annotations.Test;
import org.example.pages.HomePage;

@Slf4j
public class UploadVideo extends TestBase {
    HomePage homePage = new HomePage();

    @Test(description = "Upload Video")
    public void uploadVideo() {
     homePage.navigateToHomePage();
     
    }

}
