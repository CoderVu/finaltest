package testCase;

import org.example.core.dataProvider.DataProvider;
import org.example.pages.AgodaHomePage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import static org.example.core.assertion.retry.AwaitAssert.assertEquals;

@Slf4j
public class TC02 extends TestBase {

    AgodaHomePage homePage = new AgodaHomePage();

    @Test(description = "TC02: Search and sort hotel successfully", dataProvider = "TC01", dataProviderClass = DataProvider.class)
    public void TC02(String destination, int rooms, int adults, int children, int expectedHotelCount) {
        
        // Step 1: Navigate to https://www.agoda.com/
        homePage.navigateToHomePage();

        // Step 2: Search for hotels with specified criteria
        homePage.enterAndSelectDestination(destination);

        // Step 3: Configure dates (3 days from next Friday)
        homePage.selectDatesFromNextFriday();

        // Step 4: Configure occupancy
        homePage.SelectOccupancy(rooms, adults, children);

        // Step 5: Search
        homePage.clickSearchButton();

        // Step 6: Switch to search results tab (Agoda opens results in new tab)
        homePage.switchToSearchResultsTab();
        homePage.waitForSearchResultsToLoad();

        // Assertion with auto-retry: actual is dynamic (Supplier) - re-fetches from UI on each retry
        assertEquals(
            () -> homePage.getHotelListSize(),
            expectedHotelCount,
            "Hotel count does not match expected value"
        );
    }

}