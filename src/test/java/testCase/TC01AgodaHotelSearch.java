package testCase;

import org.example.core.assertion.MySoftAssert;
import org.example.core.dataProvider.DataProvider;
import org.example.core.dataProvider.DataFile;
import org.example.core.dataProvider.DataPath;
import org.example.models.Hotel;
import org.example.pages.AgodaHomePage;
import org.testng.annotations.Test;
import config.TestBase;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.example.core.helper.AssertionHelper.*;

@Slf4j
@Story("Hotel Search and Sort")
public class TC01AgodaHotelSearch extends TestBase {

    AgodaHomePage homePage = new AgodaHomePage();

    @Test(description = "TC01: Search and sort hotel successfully", dataProvider = "auto", dataProviderClass = DataProvider.class)
    @DataFile("tc01.json")
    @Severity(SeverityLevel.CRITICAL)
    public void TC01_SearchAndSortHotelSuccessfully(@DataPath("destination") String destination, @DataPath("occupancy.rooms") int rooms, @DataPath("occupancy.adults") int adults, @DataPath("occupancy.children") int children, @DataPath("validation.expectedHotelCount") int expectedHotelCount) {
        // Step 1: Navigate to https://www.agoda.com/
        homePage.navigateToHomePage();

        // Step 2: Search for hotels with specified criteria
        homePage.enterDestination(destination);
        homePage.selectDestinationFromSuggestions(destination);

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
        List<Hotel> hotels = homePage.getAllHotelsFromListViewSearch(expectedHotelCount);
        int actualHotelCount = homePage.getTotalHotelsCount(hotels);
        
        // Assert with auto-retry: Verify at least expected number of hotels are displayed
        assertTrue(
            () -> homePage.verifySearchResultsDisplayed(expectedHotelCount),
            String.format("Verify at least %d hotels are displayed. Found hotels: %d", 
                expectedHotelCount, actualHotelCount)
        );
        
        // Assert with auto-retry: Verify hotel count is greater than or equal to expected
        assertGreaterThanOrEqual(
            actualHotelCount, 
            expectedHotelCount,
            String.format("Hotel count should be at least %d. Actual: %d", 
                expectedHotelCount, actualHotelCount)
        );

        // Step 8: Sort hotels by lowest price
        int beforeSortCount = homePage.getHotelListSize();
        homePage.sortByLowestPrice();
        homePage.waitForPropertyCardCountChange(beforeSortCount);

        // Step 9: Verify sorting and destination with auto-retry
        // Note: Supplier will re-fetch data from UI on retry to get fresh data
        int expectedCount = Math.min(5, expectedHotelCount);
        
        // Assert with auto-retry: Verify hotels are sorted by lowest price
        // Supplier re-fetches hotels from UI on retry to ensure fresh data
        assertTrue(
            () -> {
                List<Hotel> hotelsAfterSort = homePage.getAllHotelsFromListViewSearch(expectedCount);
                return homePage.verifyHotelsSortedByLowestPrice(hotelsAfterSort);
            },
            "Verify hotels are sorted by lowest price after sort operation"
        );
        

        MySoftAssert.get().assertAll();
    }

}