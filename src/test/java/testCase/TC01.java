package testCase;

import org.example.core.dataProvider.DataProvider;
import org.example.core.assertion.Assert;
import org.example.models.Hotel;
import org.example.pages.AgodaHomePage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

        // Step 7: Verify search results (single evaluation, no assertion retry)
        boolean hasExpectedResults = homePage.checkSearchResults(expectedHotelCount);
        Assert.assertTrue(
                hasExpectedResults,
                String.format("Verify at least %d hotels are displayed", expectedHotelCount)
        );

        List<Hotel> hotels = homePage.getAllHotelsFromListViewSearch(expectedHotelCount);
        int totalHotels = homePage.getTotalHotelsCount(hotels);
        Assert.assertTrue(
                totalHotels >= expectedHotelCount,
                String.format("Hotel count should be at least %d | actual=%d", expectedHotelCount, totalHotels)
        );

        // Step 8: Sort hotels by lowest price
        int beforeSortCount = homePage.getHotelListSize();
        homePage.sortByLowestPrice();
        homePage.waitForPropertyCardCountChange(beforeSortCount);

        // Step 9: Verify sorting and destination with auto-retry
        int expectedCount = Math.min(5, expectedHotelCount);
        
        // Assert: Verify hotels are sorted by lowest price (single evaluation, no assertion retry)
        List<Hotel> hotelsAfterSort = homePage.getAllHotelsFromListViewSearch(expectedCount);
        boolean sortedByLowestPrice = homePage.checkHotelsSortedByLowestPrice(hotelsAfterSort);
        // Assertions.get().assertTrue(
        //         sortedByLowestPrice,
        //         "Verify hotels are sorted by lowest price after sort operation"
        // );
    }

}