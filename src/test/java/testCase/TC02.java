package testCase;

import org.example.core.assertion.MyAssertJ;
import org.example.core.dataProvider.DataProvider;
import org.example.core.dataProvider.DataFile;
import org.example.core.dataProvider.DataPath;
import org.example.models.Hotel;
import org.example.pages.AgodaHomePage;
import org.testng.annotations.Test;
import config.TestBase;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.example.core.helper.AssertionHelper.*;

@Slf4j
public class TC02 extends TestBase {

    AgodaHomePage homePage = new AgodaHomePage();

    @Test(description = "TC02: Search and sort hotel successfully", dataProvider = "auto", dataProviderClass = DataProvider.class)
    @DataFile("tc01.json")
    public void TC02_SearchAndSortHotelSuccessfully(@DataPath("destination") String destination, @DataPath("occupancy.rooms") int rooms, @DataPath("occupancy.adults") int adults, @DataPath("occupancy.children") int children, @DataPath("validation.expectedHotelCount") int expectedHotelCount) {
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

        //assertion helper usage
        MyAssertJ.get().assertEquals(homePage.getHotelListSize(), expectedHotelCount, "Hotel count does not match expected value.");
    }

}