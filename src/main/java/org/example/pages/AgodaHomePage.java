package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.common.imp.*;
import org.example.core.control.util.DriverUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.time.DayOfWeek;
import org.example.common.Constants;
import org.example.model.Hotel;
import java.util.stream.Collectors;
import static org.example.core.control.util.DriverUtils.getCurrentUrl;
import static com.codeborne.selenide.Selenide.sleep;

@Slf4j
public class AgodaHomePage extends BasePage {

    protected TextBox destinationSearchInput = new TextBox("//input[@data-selenium='textInput' and @placeholder='Enter a destination or property']");
    protected Element autocompletePanel = new Element("//div[@data-selenium='autocompletePanel']");
    protected Element checkInBox = new Element("//div[@data-element-name='check-in-box']");
    protected Element checkOutBox = new Element("//div[@data-element-name='check-out-box']");
    protected Element calendarContainer = new Element("//div[@id='DatePicker__AccessibleV2']");
    protected Element nextMonthButton = new Element("//button[@data-selenium='calendar-next-month-button']");
    protected Element occupancyBox = new Element("//div[@data-element-name='occupancy-box']");
    protected Element hotelListContainer = new Element("//ol[contains(@class,'hotel-list-container')]");
    protected Button searchButton = new Button("//button[@data-selenium='searchButton']");

    // XPath Locators for occupancy configuration
    protected String occupancyPopupXpathString = "//div[@class='OccupancySelector OccupancySelector--travelWithKids']";
    protected String roomValueXpath = "//div[@data-component='desktop-occ-room-value']//p";
    protected String adultValueXpath = "//div[@data-component='desktop-occ-adult-value']//p";
    protected String childrenValueXpath = "//div[@data-component='desktop-occ-children-value']//p";
    protected String roomsPlusButtonXpath = "//div[@data-selenium='occupancyRooms']//button[@data-selenium='plus']";
    protected String roomsMinusButtonXpath = "//div[@data-selenium='occupancyRooms']//button[@data-selenium='minus']";
    protected String adultsPlusButtonXpath = "//div[@data-selenium='occupancyAdults']//button[@data-selenium='plus']";
    protected String adultsMinusButtonXpath = "//div[@data-selenium='occupancyAdults']//button[@data-selenium='minus']";
    protected String childrenPlusButtonXpath = "//div[@data-selenium='occupancyChildren']//button[@data-selenium='plus']";
    protected String childrenMinusButtonXpath = "//div[@data-selenium='occupancyChildren']//button[@data-selenium='minus']";
    protected String alternativeApplyButtonXpath = "//button[contains(@class, 'occupancy') and contains(text(), 'Apply')]";

    // Updated XPath Locators for new search results structure
    protected static String propertyCardXpath = "//div[contains(@class, 'PropertyCard__Section--propertyInfo')]";
    protected static String hotelNameXpath = ".//h3[@data-selenium='hotel-name']";
    protected static String starsXpath = ".//svg[@role='img']";
    protected static String ratingXpath = ".//div[@data-testid='rating-container']";
    protected static String locationXpath = ".//button[@data-selenium='area-city-text']//span";
    protected static String cashbackXpath = ".//div[@data-selenium='cashback-badge']//span";
    protected static String amenityXpath = ".//div[@data-element-name='pill-each-item']//span";
    protected static String badgeXpath = ".//div[contains(@data-badge-id, 'pct')]//span";

    @Step("Navigate to home page")
    public void navigateToHomePage() {
        String current = getCurrentUrl();
        String base = Constants.getBaseUrl();
        if (current == null || !current.startsWith(base)) {
            DriverUtils.navigateTo(base);
        }
    }

    @Step("Enter destination: {destination}")
    public void enterDestination(String destination) {
        destinationSearchInput.clear();
        destinationSearchInput.setText(destination);
    }

    @Step("Select destination from suggestions: {destinationName}")
    public void selectDestinationFromSuggestions(String destinationName) {
        autocompletePanel.waitForVisibility();
        findElement(By.xpath(String.format("//li[@data-selenium='autosuggest-item'][@data-text='%s']", destinationName))).click();
    }

    @Step("Select check-in date: {checkInDate} and check-out date: {checkOutDate}")
    public void selectDates(String checkInDate, String checkOutDate) {
        selectCheckInDate(checkInDate);
        selectCheckOutDate(checkOutDate);
    }

    @Step("Select check-in date: {checkInDate}")
    public void selectCheckInDate(String checkInDate) {
        // 1. Mở calendar chắc chắn
        int openAttempts = 0;
        while (!calendarContainer.isVisible() && openAttempts < 5) {
            checkInBox.scrollElementToCenterScreen();


            checkInBox.clickByJs();
            try {
                calendarContainer.waitForVisibility();
            } catch (Exception e) {
                log.warn("Calendar not visible after attempt {}", openAttempts + 1);
            }
            openAttempts++;
        }

        if (!calendarContainer.isVisible()) {
            throw new RuntimeException("Calendar not visible after multiple attempts");
        }

        // 2. Select check-in date with retry if element not yet rendered
        Element checkInDateElement = new Element(
                String.format("//span[@data-selenium-date='%s']", checkInDate));
        for (int i = 0; i < 3; i++) {
            try {
                checkInDateElement.waitForElementClickable();
                checkInDateElement.clickByJs();
                log.info("Selected check-in date: {}", checkInDate);
                break;
            } catch (Exception e) {
                log.warn("Failed to select check-in date, retry {}", i + 1);
                checkInBox.click();
                try {
                    calendarContainer.waitForVisibility();
                } catch (Exception ex) {
                    log.warn("Calendar not visible during retry");
                }
            }
        }

        // 3. Đóng calendar
        if (calendarContainer.isVisible()) {
            WebElement outsideArea = findElement(By.cssSelector("body"));
            ((JavascriptExecutor) DriverUtils.getWebDriver()).executeScript("arguments[0].click();", outsideArea);
            try {
                calendarContainer.waitForInvisibility();
                log.info("Closed calendar after selecting check-in date");
            } catch (Exception e) {
                log.warn("Calendar không tự tắt sau khi click ngoài");
            }
        }
    }

    @Step("Select check-out date: {checkOutDate}")
    public void selectCheckOutDate(String checkOutDate) {
        // 1. Open calendar if not already open
        checkOutBox.waitForElementClickable();
        for (int i = 0; i < 3; i++) {
            checkOutBox.click();
            try {
                calendarContainer.waitForVisibility();
                break;
            } catch (Exception e) {
                log.warn("Calendar not visible after attempt {}", i + 1);
            }
        }

        // 2. Select check-out date with retry if element not yet rendered
        Element checkOutDateElement = new Element(
                String.format("//span[@data-selenium-date='%s']", checkOutDate));
        for (int i = 0; i < 3; i++) {
            try {
                checkOutDateElement.waitForElementClickable();
                checkOutDateElement.clickByJs();
                log.info("Selected check-out date: {}", checkOutDate);
                break;
            } catch (Exception e) {
                log.warn("Failed to select check-out date, retry {}", i + 1);
                checkOutBox.click();
                try {
                    calendarContainer.waitForVisibility();
                } catch (Exception ex) {
                    log.warn("Calendar not visible during retry");
                }
            }
        }

        // 3. Wait for calendar to close automatically (no manual intervention needed)
        try {
            calendarContainer.waitForInvisibility();
            log.info("Calendar closed automatically after selecting check-out date");
        } catch (Exception e) {
            log.info("Calendar already closed or not found - this is expected behavior");
        }
    }

    @Step("Click search button")
    public void clickSearchButton() {
        searchButton.waitForElementClickable();
        searchButton.click();
    }

    @Step("Calculate and select dates: 3 days from next Friday")
    public void selectDatesFromNextFriday() {
        LocalDate today = LocalDate.now();

        // Find next Friday
        LocalDate nextFriday = today;
        while (nextFriday.getDayOfWeek() != DayOfWeek.FRIDAY || !nextFriday.isAfter(today)) {
            nextFriday = nextFriday.plusDays(1);
        }

        // Check-in starts on next Friday
        LocalDate checkInDate = nextFriday;
        // Check-out is 3 days after check-in (3-night stay)
        LocalDate checkOutDate = checkInDate.plusDays(3);

        // Format as yyyy-MM-dd (Agoda expects this format for data-selenium-date)
        String checkInDateStr = checkInDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String checkOutDateStr = checkOutDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("Next Friday: {}", nextFriday);
        log.info("Check-in date (starting on next Friday): {}", checkInDateStr);
        log.info("Check-out date (3 nights later): {}", checkOutDateStr);

        // Select the calculated dates
        selectDates(checkInDateStr, checkOutDateStr);
    }

    @Step("Select occupancy: {rooms} rooms, {adults} adults, {children} children")
    public void SelectOccupancy(int rooms, int adults, int children) {
        // Check if occupancy popup is already open (it might auto-open after date selection)
        Element occupancyPopup = new Element(occupancyPopupXpathString);

        try {
            // Wait a short time to see if popup is already visible
            occupancyPopup.waitForVisibility();
            log.info("Occupancy popup is already open");
        } catch (Exception e) {
            // Popup is not visible, need to click to open it
            log.info("Occupancy popup not visible, clicking to open");
            occupancyBox.waitForElementClickable();
            occupancyBox.click();
            // Now wait for popup to appear after clicking
            occupancyPopup.waitForVisibility();
        }

        log.info("Configuring occupancy: {} rooms, {} adults, {} children", rooms, adults, children);

        // Configure rooms
        selectRooms(rooms);

        // Select adults
        selectAdults(adults);

        // Select children
        selectChildren(children);
    }

    @Step("Select rooms to: {targetRooms}")
    private void selectRooms(int targetRooms) {
        Element roomValueElement = new Element(roomValueXpath);
        roomValueElement.waitForVisibility();

        int currentRooms = Integer.parseInt(roomValueElement.getText().trim());
        log.info("Current rooms: {}, Target rooms: {}", currentRooms, targetRooms);

        Button roomsPlusButton = new Button(roomsPlusButtonXpath);
        Button roomsMinusButton = new Button(roomsMinusButtonXpath);

        while (currentRooms < targetRooms) {
            roomsPlusButton.waitForElementClickable();
            roomsPlusButton.click();
            currentRooms++;
            log.info("Increased rooms to: {}", currentRooms);
        }

        while (currentRooms > targetRooms) {
            if (roomsMinusButton.getAttribute("disabled") != null) {
                log.warn("Cannot decrease rooms further - minimum reached");
                break;
            }
            roomsMinusButton.waitForElementClickable();
            roomsMinusButton.click();
            currentRooms--;
            log.info("Decreased rooms to: {}", currentRooms);
        }
    }

    @Step("Select adults to: {targetAdults}")
    private void selectAdults(int targetAdults) {
        Element adultValueElement = new Element(adultValueXpath);
        adultValueElement.waitForVisibility();

        int currentAdults = Integer.parseInt(adultValueElement.getText().trim());
        log.info("Current adults: {}, Target adults: {}", currentAdults, targetAdults);

        Button adultsPlusButton = new Button(adultsPlusButtonXpath);
        Button adultsMinusButton = new Button(adultsMinusButtonXpath);

        while (currentAdults < targetAdults) {
            adultsPlusButton.waitForElementClickable();
            adultsPlusButton.click();
            currentAdults++;
            log.info("Increased adults to: {}", currentAdults);
        }

        while (currentAdults > targetAdults) {
            if (adultsMinusButton.getAttribute("disabled") != null) {
                log.warn("Cannot decrease adults further - minimum reached");
                break;
            }
            adultsMinusButton.waitForElementClickable();
            adultsMinusButton.click();
            currentAdults--;
            log.info("Decreased adults to: {}", currentAdults);
        }
    }

    @Step("Select children to: {targetChildren}")
    private void selectChildren(int targetChildren) {
        Element childrenValueElement = new Element(childrenValueXpath);
        childrenValueElement.waitForVisibility();

        int currentChildren = Integer.parseInt(childrenValueElement.getText().trim());
        log.info("Current children: {}, Target children: {}", currentChildren, targetChildren);

        Button childrenPlusButton = new Button(childrenPlusButtonXpath);
        Button childrenMinusButton = new Button(childrenMinusButtonXpath);

        while (currentChildren < targetChildren) {
            childrenPlusButton.waitForElementClickable();
            childrenPlusButton.click();
            currentChildren++;
            log.info("Increased children to: {}", currentChildren);
        }

        while (currentChildren > targetChildren) {
            if (childrenMinusButton.getAttribute("disabled") != null) {
                log.warn("Cannot decrease children further - minimum reached");
                break;
            }
            childrenMinusButton.waitForElementClickable();
            childrenMinusButton.click();
            currentChildren--;
            log.info("Decreased children to: {}", currentChildren);
        }
    }

    @Step("Get hotel information from search results")
    public List<Hotel> getAllHotelsFromListViewSearch() {
        List<Hotel> hotels = new ArrayList<>();
        try {
            List<Element> hotelCards = hotelListContainer.getListElements(Element.class, propertyCardXpath);
            log.info("Found {} hotel cards in search results", hotelCards.size());

            for (int i = 0; i < hotelCards.size(); i++) {
                try {
                    Element cardElement = hotelCards.get(i);
                    cardElement.scrollToView();
                    Hotel hotel = extractHotelFromCard(cardElement);
                    if (hotel != null) {
                        hotels.add(hotel);
                        log.info("Hotel {}: {}", i + 1, hotel.toString());
                    }
                } catch (Exception e) {
                    log.warn("Error extracting hotel info at index {}: {}", i, e.getMessage());
                }
            }

            log.info("Successfully extracted {} hotels information", hotels.size());
            return hotels;

        } catch (Exception e) {
            log.error("Error getting hotel information: {}", e.getMessage());
            return hotels;
        }
    }

    private Hotel extractHotelFromCard(Element card) {
        try {
            Hotel.HotelBuilder hotelBuilder = Hotel.builder();

            // Extract hotel name
            try {
                Element nameElement = new Element(card, hotelNameXpath);
                hotelBuilder.name(nameElement.getText().trim());
            } catch (Exception e) {
                log.warn("Could not find hotel name");
                return null;
            }

            // Extract rating
            try {
                Element ratingContainer = new Element(card, ratingXpath);
                List<WebElement> stars = ratingContainer.getChildElements(starsXpath);
                hotelBuilder.rating(stars.size() + " stars");
            } catch (Exception e) {
                log.debug("Could not find rating");
            }

            // Extract location and distance
            try {
                Element locationElement = new Element(card, locationXpath);
                String locationText = locationElement.getText().trim();
                String[] locationParts = locationText.split(" - ");
                if (locationParts.length >= 2) {
                    hotelBuilder.location(locationParts[0]);
                    hotelBuilder.distanceToCenter(locationParts[1]);
                } else {
                    hotelBuilder.location(locationText);
                }
            } catch (Exception e) {
                log.debug("Could not find location");
            }

            // Extract cashback reward
            try {
                Element cashbackElement = new Element(card, cashbackXpath);
                hotelBuilder.cashbackReward(cashbackElement.getText().trim());
            } catch (Exception e) {
                log.debug("Could not find cashback");
            }

            // Extract amenities
            try {
                List<WebElement> amenityElements = card.getChildElements(amenityXpath);
                String[] amenities = amenityElements.stream()
                        .map(element -> element.getText().trim())
                        .filter(text -> !text.isEmpty() && !text.startsWith("+"))
                        .toArray(String[]::new);
                hotelBuilder.amenities(amenities);
            } catch (Exception e) {
                log.debug("Could not find amenities");
            }

            // Extract badges
            try {
                List<WebElement> badgeElements = card.getChildElements(badgeXpath);
                String[] badges = badgeElements.stream()
                        .map(element -> element.getText().trim())
                        .filter(text -> !text.isEmpty())
                        .toArray(String[]::new);
                hotelBuilder.badges(badges);
            } catch (Exception e) {
                log.debug("Could not find badges");
            }

            return hotelBuilder.build();

        } catch (Exception e) {
            log.error("Error extracting hotel info: {}", e.getMessage());
            return null;
        }
    }

    @Step("Verify search results are displayed correctly with at least {expectedCount} hotels")
    public boolean verifySearchResultsDisplayed(int expectedCount) {
        try {
            List<Hotel> hotels = getAllHotelsFromListViewSearch();
            int actualCount = hotels.size();

            log.info("Found {} hotels in search results", actualCount);

            // Check if we have at least the expected number of hotels
            if (actualCount < expectedCount) {
                log.error("Expected at least {} hotels, but found only {}", expectedCount, actualCount);
                return false;
            }

            // Verify that hotels have valid names
            long validHotels = hotels.stream()
                    .filter(hotel -> hotel.getName() != null && !hotel.getName().isEmpty())
                    .count();

            if (validHotels < expectedCount) {
                log.error("Expected at least {} valid hotels, but found only {}", expectedCount, validHotels);
                return false;
            }

            log.info("Search results validation successful: {} valid hotels", validHotels);
            return true;

        } catch (Exception e) {
            log.error("Error verifying search results: {}", e.getMessage());
            return false;
        }
    }

    @Step("Get total hotels count from list")
    public int getTotalHotelsCount(List<Hotel> hotels) {
        return hotels.size(); 
    }

    @Step("Switch to search results tab")
    public void switchToSearchResultsTab() {
        // Wait for new window to open (expecting 2 windows total)
        DriverUtils.waitForNewWindowOpened(2);

        // Switch to the new window (index 1, since original is index 0)
        DriverUtils.switchToWindow(1);

        // Wait for search results URL to load
        DriverUtils.waitForUrlContains("search", DriverUtils.getTimeOut());
    }

    @Step("Wait for search results to load")
    public void waitForSearchResultsToLoad() {
        try {
            // Wait for at least one property card to be visible
            Element firstPropertyCard = new Element(propertyCardXpath + "[1]");
            firstPropertyCard.waitForVisibility(10);

            log.info("Search results have loaded successfully");
        } catch (Exception e) {
            log.error("Failed to wait for search results: {}", e.getMessage());
        }
    }
}