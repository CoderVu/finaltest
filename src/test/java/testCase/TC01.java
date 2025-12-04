package testCase;

import org.example.core.dataProvider.DataProvider;
import org.example.models.Hotel;
import org.example.pages.AgodaHomePage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.example.core.assertion.retry.FunctionAssertions.*;

@Slf4j
public class TC01 extends TestBase {

    AgodaHomePage homePage = new AgodaHomePage();

    @Test(description = "TC01: Search and sort hotel successfully", dataProvider = "TC01", dataProviderClass = DataProvider.class)
    public void TC01(String destination, int rooms, int adults, int children, int expectedHotelCount) {
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

        // Step 7: Verify search results with auto-retry assertions
        // Assert with auto-retry: Verify at least expected number of hotels are displayed
        assertTrue(
            () -> homePage.checkSearchResults(expectedHotelCount),
            String.format("Verify at least %d hotels are displayed", expectedHotelCount)
        );
        
        // Assert with auto-retry: Verify hotel count is greater than or equal to expected
        // actual is dynamic (Supplier) - re-fetches from UI on each retry
        assertGreaterThanOrEqual(
            () -> {
                List<Hotel> hotels = homePage.getAllHotelsFromListViewSearch(expectedHotelCount);
                return homePage.getTotalHotelsCount(hotels);
            },
            expectedHotelCount,
            String.format("Hotel count should be at least %d", expectedHotelCount)
        );

        // Step 8: Sort hotels by lowest price
        int beforeSortCount = homePage.getHotelListSize();
        homePage.sortByLowestPrice();
        homePage.waitForPropertyCardCountChange(beforeSortCount);

        // Step 9: Verify sorting and destination with auto-retry
        int expectedCount = Math.min(5, expectedHotelCount);
        
        // Assert: Verify hotels are sorted by lowest price
        assertTrue(
            () -> {
                List<Hotel> hotelsAfterSort = homePage.getAllHotelsFromListViewSearch(expectedCount);
                return homePage.checkHotelsSortedByLowestPrice(hotelsAfterSort);
            },
            "Verify hotels are sorted by lowest price after sort operation"
        );
    }

}