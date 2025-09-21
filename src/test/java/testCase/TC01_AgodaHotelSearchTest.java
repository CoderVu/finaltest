package testCase;

import config.TestBase;
import io.qameta.allure.*;
import lombok.extern.slf4j.Slf4j;

import org.example.pages.AgodaHomePage;
import org.example.report.SoftAssertConfig;
import org.testng.annotations.*;

import java.util.List;

import org.example.model.Hotel;

@Slf4j
@Epic("Hotel Booking")
@Story("Hotel Search and Sort")
public class TC01_AgodaHotelSearchTest extends TestBase {
    
    AgodaHomePage homePage = new AgodaHomePage();
    SoftAssertConfig softAssert = new SoftAssertConfig();
    // Test data constants
    private final String DESTINATION = "Da Nang";
    private final int ROOMS = 2;
    private final int ADULTS = 4;
    private final int CHILDREN = 0;
    private final int EXPECTED_HOTEL_COUNT = 5;

    @Test(description = "TC01: Search and sort hotel successfully")
    @Story("Hotel Search and Sort")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Navigate to Agoda, search for hotels in Da Nang with specific criteria, and sort by lowest price")
    public void TC01_SearchAndSortHotelSuccessfully() {

        // Step 1: Navigate to https://www.agoda.com/
        homePage.navigateToHomePage();

        // Step 2: Search for hotels with specified criteria
        homePage.enterDestination(DESTINATION);
        homePage.selectDestinationFromSuggestions(DESTINATION);
        
        // Step 3: Configure dates (3 days from next Friday)
        homePage.selectDatesFromNextFriday();
        
        // Step 4: Configure occupancy (2 rooms, 4 adults)
        homePage.SelectOccupancy(ROOMS, ADULTS, CHILDREN);
        
        // Step 5: Search
        homePage.clickSearchButton();
        
        // Step 6: Switch to search results tab (Agoda opens results in new tab)
        homePage.switchToSearchResultsTab();
        homePage.waitForSearchResultsToLoad();

        // Step 7: Verify search results
        List<Hotel> hotels = homePage.getAllHotelsFromListViewSearch();
        softAssert.assertTrue(homePage.verifySearchResultsDisplayed(EXPECTED_HOTEL_COUNT),
            String.format("Verify at least %d hotels are displayed. Found hotels: %d",
                EXPECTED_HOTEL_COUNT, homePage.getTotalHotelsCount(hotels)));

        softAssert.assertAll();
    }

}