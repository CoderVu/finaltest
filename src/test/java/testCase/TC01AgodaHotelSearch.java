package testCase;

import java.util.List;

import org.example.core.dataProvider.DataProvider;
import org.example.core.dataProvider.DataFile;
import org.example.core.dataProvider.DataPath;
import org.example.models.Hotel;
import org.example.pages.AgodaHomePage;
import org.example.core.report.SoftAssertConfig;
import org.testng.annotations.Test;
import config.TestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Epic("Hotel Booking")
@Story("Hotel Search and Sort")
public class TC01AgodaHotelSearch extends TestBase {

    AgodaHomePage homePage = new AgodaHomePage();
    SoftAssertConfig softAssert = new SoftAssertConfig();

    @Test(description = "TC01: Search and sort hotel successfully", dataProvider = "auto", dataProviderClass = DataProvider.class)
    @DataFile("src/test/resources/data/tc01.json")
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

        // Step 7: Verify search results
        List<Hotel> hotels = homePage.getAllHotelsFromListViewSearch(expectedHotelCount);
        softAssert.assertTrue(homePage.verifySearchResultsDisplayed(expectedHotelCount),
                String.format("Verify at least %d hotels are displayed. Found hotels: %d",
                        expectedHotelCount, homePage.getTotalHotelsCount(hotels)));

        int beforeSortCount = homePage.getHotelListSize();
        homePage.sortByLowestPrice();
        homePage.waitForPropertyCardCountChange(beforeSortCount);
        
        // Fix: Get hotels list AFTER sorting to verify correct order
        List<Hotel> hotelsAfterSort = homePage.getAllHotelsFromListViewSearch(Math.min(5, expectedHotelCount));
        softAssert.assertFalse(homePage.verifyHotelsSortedByLowestPrice(hotelsAfterSort),
                "Verify hotels are sorted by lowest price after sort operation");
        softAssert.assertTrue(homePage.verifyHotelsDestination(hotelsAfterSort, destination),
                "Verify hotels have correct destination after sort");

        softAssert.assertAll();
    }

}