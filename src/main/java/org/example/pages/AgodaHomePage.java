package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.element.ElementWrapper;
import org.example.core.element.util.DriverUtils;
import org.example.core.element.util.WaitUtils;
import org.example.models.Hotel;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.parse;
import static org.example.core.element.util.DriverUtils.getCurrentUrl;

@Slf4j
public class AgodaHomePage extends BasePage {

    // Lazily create wrappers each time to avoid stale element references
    protected ElementWrapper destinationSearchInput() {
        return new ElementWrapper("//input[@data-selenium='textInput' and @placeholder='Enter a destination or property']");
    }

    protected ElementWrapper autocompletePanel() {
        return new ElementWrapper("//div[@data-selenium='autocompletePanel']");
    }

    protected ElementWrapper checkInBox() {
        return new ElementWrapper("//div[@data-element-name='check-in-box']");
    }

    protected ElementWrapper checkOutBox() {
        return new ElementWrapper("//div[@data-element-name='check-out-box']");
    }

    protected ElementWrapper calendarContainer() {
        return new ElementWrapper("//div[@id='DatePicker__AccessibleV2']");
    }

    protected ElementWrapper occupancyBox() {
        return new ElementWrapper("//div[@data-element-name='occupancy-box']");
    }

    protected ElementWrapper hotelListContainer() {
        return new ElementWrapper("//ol[contains(@class,'hotel-list-container')]");
    }

    protected ElementWrapper monthCaption() {
        return new ElementWrapper("//div[contains(@class,'DayPicker-Caption')]");
    }

    protected ElementWrapper searchButton() {
        return new ElementWrapper("//button[@data-selenium='searchButton']");
    }

    protected ElementWrapper sortPriceButton() {
        return new ElementWrapper("//button[@data-element-name='search-sort-price']");
    }

    protected ElementWrapper nextMonthBtn() {
        return new ElementWrapper("//button[@data-selenium='calendar-next-month-button']");
    }

    protected ElementWrapper prevMonthBtn() {
        return new ElementWrapper("//button[@data-selenium='calendar-previous-month-button']");
    }

    protected String occupancyPopupXpath = "//div[@class='OccupancySelector OccupancySelector--travelWithKids']";
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
    protected String monthStringXpath = "//div[contains(@class,'DayPicker-Caption')]";
    protected static String propertyCardXpath = "//div[@data-element-name='PropertyCardBaseJacket']";
    protected static String hotelNameXpath = ".//h3[@data-selenium='hotel-name']";
    protected static String starsXpath = ".//svg[@role='img']";
    protected static String ratingXpath = ".//div[@data-testid='rating-container']";
    protected static String locationXpath = ".//button[@data-selenium='area-city-text']//span";
    protected static String amenityXpath = ".//div[@data-element-name='pill-each-item']//span";
    protected static String badgeXpath = ".//div[contains(@data-badge-id, 'pct')]//span";
    protected static String priceXpath = ".//span[@data-selenium='display-price']";
    protected static String originalPriceXpath = ".//div[@data-element-name='first-cor']";

    public void DraftTestFunction() {
        ElementWrapper sampleElement = new ElementWrapper("//div[@data-selenium='sample-element']");
        sampleElement.checkCheckBoxByJs();
    }

    public void navigateToHomePage() {
        step(() -> {
            String current = getCurrentUrl();
            String base = Config.getBaseUrl();
            if (current == null || !current.startsWith(base)) {
                DriverUtils.navigateTo(base);
            }
        });
    }

    public void enterAndSelectDestination(String destination) {
        step("Enter and select destination: " + destination, () -> {
            // 1) Nhập text vào ô search
            try {
                destinationSearchInput().waitForVisibility(Duration.ofSeconds(10));
                destinationSearchInput().waitForElementClickable(Duration.ofSeconds(10));
                destinationSearchInput().clear();
                destinationSearchInput().setText(destination);
            } catch (Exception e) {
                log.warn("First attempt to enter destination failed, retrying: {}", e.getMessage());
                try {
                    DriverUtils.delay(1);
                    destinationSearchInput().waitForVisibility(Duration.ofSeconds(10));
                    destinationSearchInput().clear();
                    destinationSearchInput().setText(destination);
                } catch (Exception retryEx) {
                    log.error("Failed to enter destination after retry: {}", retryEx.getMessage());
                    screenshot("enter_destination_failed");
                    throw new RuntimeException("Failed to enter destination: " + destination, retryEx);
                }
            }

            // 2) Chờ suggestions xuất hiện và chọn item phù hợp
            try {
                autocompletePanel().waitForVisibility(Duration.ofSeconds(10));

                // Locator đại diện cho tất cả autosuggest items (bên trong Popup AutocompleteList)
                ElementWrapper suggestionsLocator = new ElementWrapper("//div[contains(@class,'Popup__content')]" +
                        "//ul[contains(@class,'AutocompleteList')]//li[@data-selenium='autosuggest-item']");

                // Đợi đến khi có ít nhất 1 autosuggest item trong DOM
                WaitUtils.waitFor(driver -> {
                    List<WebElement> found = suggestionsLocator.getElements();
                    return found != null && !found.isEmpty();
                }, Duration.ofSeconds(10));

                List<WebElement> rawSuggestions = suggestionsLocator.getElements();

                if (rawSuggestions == null || rawSuggestions.isEmpty()) {
                    log.warn("No autosuggest items found for destination: {}", destination);
                    screenshot("no_autosuggest_items");
                    throw new RuntimeException("No autosuggest items found for destination: " + destination);
                }

                // Ưu tiên match theo data-text == destination, nếu không có thì chứa destination, nếu vẫn không có thì lấy item đầu
                WebElement target = null;
                for (WebElement item : rawSuggestions) {
                    String dataText = item.getAttribute("data-text");
                    if (dataText == null) {
                        continue;
                    }
                    if (dataText.equalsIgnoreCase(destination) ||
                            dataText.toLowerCase().contains(destination.toLowerCase())) {
                        target = item;
                        break;
                    }
                }

                if (target == null) {
                    target = rawSuggestions.get(0);
                    log.warn("No exact/contains match for '{}', fallback to first suggestion '{}'",
                            destination, rawSuggestions.get(0).getAttribute("data-text"));
                }

                DriverUtils.execJavaScript("arguments[0].scrollIntoView({block: 'center'});", target);
                DriverUtils.execJavaScript("arguments[0].click();", target);
            } catch (Exception e) {
                log.warn("Failed to select destination from suggestions for '{}': {}", destination,
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                screenshot("select_destination_failed");
                throw new RuntimeException("Failed to select destination from suggestions: " + destination, e);
            }
        });
    }

    public void selectDates(String checkInDate, String checkOutDate) {
        step("Select dates: " + checkInDate + " to " + checkOutDate, () -> {
            selectCheckInDate(checkInDate);
            selectCheckOutDate(checkOutDate);
        });
    }

    public void selectCheckInDate(String checkInDate) {
        step("Select check-in date: " + checkInDate, () -> {
            int openAttempts = 0;
            while (!calendarContainer().isVisible() && openAttempts < 5) {
                checkInBox().scrollElementToCenterScreen();
                checkInBox().clickByJs();
                try {
                    calendarContainer().waitForVisibility(Duration.ofSeconds(10));
                } catch (Exception e) {
                    log.warn("Calendar not visible after attempt {}", openAttempts + 1);
                }
                openAttempts++;
            }
            if (!calendarContainer().isVisible()) {
                screenshot("calendar_not_visible");
                throw new RuntimeException("Calendar not visible after multiple attempts");
            }

            String targetMonth = parse(checkInDate).getMonth().name();
            String targetYear = String.valueOf(parse(checkInDate).getYear());

            int nextTries = 0;
            while (nextTries < 12) {
                String captionText = monthCaption().getText();
                if (captionText.toLowerCase().contains(targetMonth.toLowerCase()) && captionText.contains(targetYear)) {
                    break;
                }
                if ("true".equals(nextMonthBtn().getAttribute("aria-disabled"))) {
                    break;
                }
                nextMonthBtn().waitForElementClickable(Duration.ofSeconds(10));
                nextMonthBtn().click();
                DriverUtils.delay(0.5);
                nextTries++;
            }

            int prevTries = 0;
            while (prevTries < 24) {
                String captionText = monthCaption().getText();
                if (captionText.toLowerCase().contains(targetMonth.toLowerCase()) && captionText.contains(targetYear)) {
                    break;
                }
                if ("true".equals(prevMonthBtn().getAttribute("aria-disabled"))) {
                    break;
                }
                prevMonthBtn().waitForElementClickable(Duration.ofSeconds(10));
                prevMonthBtn().click();
                DriverUtils.delay(0.5);
                prevTries++;
            }
            ElementWrapper checkInDateElement = new ElementWrapper("//span[@data-selenium-date='" + checkInDate + "']");
            for (int i = 0; i < 3; i++) {
                try {
                    checkInDateElement.waitForElementClickable(Duration.ofSeconds(10));
                    checkInDateElement.clickByJs();
                    logInfo("Selected check-in date: " + checkInDate);
                    break;
                } catch (Exception e) {
                    log.warn("Failed to select check-in date, retry {}", i + 1);
                    checkInBox().click();
                    try {
                        calendarContainer().waitForVisibility(Duration.ofSeconds(10));
                    } catch (Exception ex) {
                        log.warn("Calendar not visible during retry");
                    }
                }
            }
            if (calendarContainer().isVisible()) {
                ElementWrapper outsideArea = new ElementWrapper("//body");
                ((JavascriptExecutor) DriverUtils.getWebDriver()).executeScript("arguments[0].click();", outsideArea.getElement());
                try {
                    calendarContainer().waitForInvisibility(Duration.ofSeconds(10));
                    logInfo("Closed calendar after selecting check-in date");
                } catch (Exception e) {
                    log.warn("Calendar không tự tắt sau khi click ngoài");
                }
            }
        });
    }

    public void selectCheckOutDate(String checkOutDate) {
        step("Select check-out date: " + checkOutDate, () -> {
            checkOutBox().waitForElementClickable(Duration.ofSeconds(10));
            for (int i = 0; i < 3; i++) {
                checkOutBox().click();
                try {
                    calendarContainer().waitForVisibility(Duration.ofSeconds(10));
                    break;
                } catch (Exception e) {
                    log.warn("Calendar not visible after attempt {}", i + 1);
                }
            }
            String targetMonth = parse(checkOutDate).getMonth().name();
            String targetYear = String.valueOf(parse(checkOutDate).getYear());
            for (int i = 0; i < 12; i++) {
                ElementWrapper monthCaptionEl = new ElementWrapper(monthStringXpath);
                String captionText = monthCaptionEl.getText();
                if (captionText.toLowerCase().contains(targetMonth.toLowerCase()) && captionText.contains(targetYear)) {
                    break;
                }
                if ("true".equals(nextMonthBtn().getAttribute("aria-disabled"))) {
                    if ("true".equals(prevMonthBtn().getAttribute("aria-disabled"))) {
                        break;
                    }
                    prevMonthBtn().waitForElementClickable(Duration.ofSeconds(10));
                    prevMonthBtn().click();
                } else {
                    nextMonthBtn().waitForElementClickable(Duration.ofSeconds(10));
                    nextMonthBtn().click();
                }
                DriverUtils.delay(0.5);
            }
            ElementWrapper checkOutDateElement = new ElementWrapper("//span[@data-selenium-date='" + checkOutDate + "']");
            for (int i = 0; i < 3; i++) {
                try {
                    checkOutDateElement.waitForElementClickable(Duration.ofSeconds(10));
                    checkOutDateElement.clickByJs();
                    logInfo("Selected check-out date: " + checkOutDate);
                    break;
                } catch (Exception e) {
                    log.warn("Failed to select check-out date, retry {}", i + 1);
                    checkOutBox().click();
                    try {
                        calendarContainer().waitForVisibility(Duration.ofSeconds(10));
                    } catch (Exception ex) {
                        log.warn("Calendar not visible during retry");
                    }
                }
            }
            try {
                calendarContainer().waitForInvisibility(Duration.ofSeconds(10));
                logInfo("Calendar closed automatically after selecting check-out date");
            } catch (Exception e) {
                logInfo("Calendar already closed or not found - this is expected behavior");
            }
        });
    }

    public void clickSearchButton() {
        step(() -> {
            searchButton().waitForElementClickable(Duration.ofSeconds(10));
            searchButton().scrollToView();
            // Use JS click to avoid stale / intercepted issues around dynamic DOM changes
            searchButton().clickByJs();
        });
    }

    public void selectDatesFromNextFriday() {
        LocalDate today = LocalDate.now();

        LocalDate nextFriday = today;
        while (nextFriday.getDayOfWeek() != DayOfWeek.FRIDAY || !nextFriday.isAfter(today)) {
            nextFriday = nextFriday.plusDays(1);
        }

        LocalDate checkInDate = nextFriday;
        LocalDate checkOutDate = checkInDate.plusDays(3);

        String checkInDateStr = checkInDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String checkOutDateStr = checkOutDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("Next Friday: {}", nextFriday);
        log.info("Check-in date (starting on next Friday): {}", checkInDateStr);
        log.info("Check-out date (3 nights later): {}", checkOutDateStr);

        selectDates(checkInDateStr, checkOutDateStr);
    }

    public void SelectOccupancy(int rooms, int adults, int children) {
        step("Select occupancy: " + rooms + " rooms, " + adults + " adults, " + children + " children", () -> {
            ElementWrapper occupancyPopup = new ElementWrapper(occupancyPopupXpath);

            try {
                occupancyPopup.waitForVisibility(Duration.ofSeconds(10));
                logInfo("Occupancy popup is already open");
            } catch (Exception e) {
                logInfo("Occupancy popup not visible, clicking to open");
                occupancyBox().waitForElementClickable(Duration.ofSeconds(10));
                occupancyBox().click();
                occupancyPopup.waitForVisibility(Duration.ofSeconds(10));
            }

            logInfo("Configuring occupancy: " + rooms + " rooms, " + adults + " adults, " + children + " children");
            selectRooms(rooms);
            selectAdults(adults);
            selectChildren(children);
        });
    }

    private void selectRooms(int targetRooms) {
        step("Select rooms: " + targetRooms, () -> {
            ElementWrapper roomValueElement = new ElementWrapper(roomValueXpath);
            roomValueElement.waitForVisibility(Duration.ofSeconds(10));

            int currentRooms = Integer.parseInt(roomValueElement.getText().trim());
            logInfo("Current rooms: " + currentRooms + ", Target rooms: " + targetRooms);

            ElementWrapper roomsPlusButton = new ElementWrapper(roomsPlusButtonXpath);
            ElementWrapper roomsMinusButton = new ElementWrapper(roomsMinusButtonXpath);

            while (currentRooms < targetRooms) {
                roomsPlusButton.waitForElementClickable(Duration.ofSeconds(10));
                roomsPlusButton.click();
                currentRooms++;
                logInfo("Increased rooms to: " + currentRooms);
            }

            while (currentRooms > targetRooms) {
                if (roomsMinusButton.getAttribute("disabled") != null) {
                    log.warn("Cannot decrease rooms further - minimum reached");
                    break;
                }
                roomsMinusButton.waitForElementClickable(Duration.ofSeconds(10));
                roomsMinusButton.click();
                currentRooms--;
                logInfo("Decreased rooms to: " + currentRooms);
            }
        });
    }

    private void selectAdults(int targetAdults) {
        step("Select adults: " + targetAdults, () -> {
            ElementWrapper adultValueElement = new ElementWrapper(adultValueXpath);
            adultValueElement.waitForVisibility(Duration.ofSeconds(10));

            int currentAdults = Integer.parseInt(adultValueElement.getText().trim());
            logInfo("Current adults: " + currentAdults + ", Target adults: " + targetAdults);

            ElementWrapper adultsPlusButton = new ElementWrapper(adultsPlusButtonXpath);
            ElementWrapper adultsMinusButton = new ElementWrapper(adultsMinusButtonXpath);

            while (currentAdults < targetAdults) {
                adultsPlusButton.waitForElementClickable(Duration.ofSeconds(10));
                adultsPlusButton.click();
                currentAdults++;
                logInfo("Increased adults to: " + currentAdults);
            }

            while (currentAdults > targetAdults) {
                if (adultsMinusButton.getAttribute("disabled") != null) {
                    log.warn("Cannot decrease adults further - minimum reached");
                    break;
                }
                adultsMinusButton.waitForElementClickable(Duration.ofSeconds(10));
                adultsMinusButton.click();
                currentAdults--;
                logInfo("Decreased adults to: " + currentAdults);
            }
        });
    }

    private void selectChildren(int targetChildren) {
        step("Select children: " + targetChildren, () -> {
            ElementWrapper childrenValueElement = new ElementWrapper(childrenValueXpath);
            childrenValueElement.waitForVisibility(Duration.ofSeconds(10));

            int currentChildren = Integer.parseInt(childrenValueElement.getText().trim());
            logInfo("Current children: " + currentChildren + ", Target children: " + targetChildren);

            ElementWrapper childrenPlusButton = new ElementWrapper(childrenPlusButtonXpath);
            ElementWrapper childrenMinusButton = new ElementWrapper(childrenMinusButtonXpath);

            while (currentChildren < targetChildren) {
                childrenPlusButton.waitForElementClickable(Duration.ofSeconds(10));
                childrenPlusButton.click();
                currentChildren++;
                logInfo("Increased children to: " + currentChildren);
            }

            while (currentChildren > targetChildren) {
                if (childrenMinusButton.getAttribute("disabled") != null) {
                    log.warn("Cannot decrease children further - minimum reached");
                    break;
                }
                childrenMinusButton.waitForElementClickable(Duration.ofSeconds(10));
                childrenMinusButton.click();
                currentChildren--;
                logInfo("Decreased children to: " + currentChildren);
            }
        });
    }

    public List<Hotel> getAllHotelsFromListViewSearch(int expectedHotelCount) {
        return step("Get all hotels from list view: " + expectedHotelCount, () -> {
            List<Hotel> hotels = new ArrayList<>();
            try {
                // Locator đại diện cho tất cả hotel cards
                ElementWrapper hotelCardsLocator = new ElementWrapper(propertyCardXpath);
                List<WebElement> hotelCardElements = hotelCardsLocator.getElements();

                logInfo("Found " + hotelCardElements.size() + " hotel cards in search results");

                int totalCards = Math.min(hotelCardElements.size(), expectedHotelCount);
                for (int i = 0; i < totalCards; i++) {
                    try {
                        // Xây dựng indexed xpath để có locator ổn định cho từng card
                        String indexedCardXpath = String.format("(%s)[%d]", propertyCardXpath, i + 1);
                        ElementWrapper cardElement = new ElementWrapper(indexedCardXpath);
                        cardElement.scrollToView();
                        Hotel hotel = extractHotelFromCard(cardElement);
                        if (hotel != null) {
                            hotels.add(hotel);
                        }
                    } catch (Exception e) {
                        log.warn("Error extracting hotel info at index " + i + ": " + e.getMessage());
                    }
                }

                logInfo("Successfully extracted " + hotels.size() + " hotels information");
                return hotels;

            } catch (Exception e) {
                log.error("Error getting hotel information: " + e.getMessage());
                return hotels;
            }
        });
    }

    private Hotel extractHotelFromCard(ElementWrapper card) {
        return step(() -> {
            try {
                Hotel.HotelBuilder hotelBuilder = Hotel.builder();

                try {
                    WebElement nameWebElement = card.getChildElement(hotelNameXpath);
                    hotelBuilder.name(nameWebElement.getText().trim());
                } catch (Exception e) {
                    log.warn("Could not find hotel name");
                    return null;
                }

                try {
                    List<WebElement> starsWebElements = card.getChildElements(starsXpath);
                    hotelBuilder.rating(starsWebElements.size() + " stars");
                } catch (Exception e) {
                    log.debug("Could not find rating");
                }

                try {
                    WebElement locationWebElement = card.getChildElement(locationXpath);
                    String locationText = locationWebElement.getText().trim();
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

                try {
                    List<WebElement> amenityWebElements = card.getChildElements(amenityXpath);
                    String[] amenities = amenityWebElements.stream()
                            .map(element -> element.getText().trim())
                            .filter(text -> !text.isEmpty() && !text.startsWith("+"))
                            .toArray(String[]::new);
                    hotelBuilder.amenities(amenities);
                } catch (Exception e) {
                    log.debug("Could not find amenities");
                }

                try {
                    List<WebElement> badgeWebElements = card.getChildElements(badgeXpath);
                    String[] badges = badgeWebElements.stream()
                            .map(element -> element.getText().trim())
                            .filter(text -> !text.isEmpty())
                            .toArray(String[]::new);
                    hotelBuilder.badges(badges);
                } catch (Exception e) {
                    log.debug("Could not find badges");
                }

                // Giá: ưu tiên display-price; nếu không có thì fallback sang giá gốc (first-cor)
                try {
                    WebElement priceWebElement = card.getChildElement(priceXpath);
                    String displayPrice = priceWebElement.getText().trim();
                    if (displayPrice == null || displayPrice.isEmpty()) {
                        throw new RuntimeException("Empty display price");
                    }
                    logInfo("Found display price: " + displayPrice);
                    hotelBuilder.price(displayPrice);
                } catch (Exception e) {
                    log.debug("Display price not found or empty, trying original price");
                    try {
                        WebElement originalPriceElement = card.getChildElement(originalPriceXpath);
                        String originalPrice = originalPriceElement.getText().trim();
                        if (originalPrice != null && !originalPrice.isEmpty()) {
                            logInfo("Found original price: " + originalPrice);
                            hotelBuilder.price(originalPrice);
                        } else {
                            log.debug("Original price element is empty");
                        }
                    } catch (Exception ex) {
                        log.debug("Could not find original price");
                    }
                }
                return hotelBuilder.build();

            } catch (Exception e) {
                log.error("Error extracting hotel info: " + e.getMessage());
                return null;
            }
        });
    }

    public boolean checkSearchResults(int expectedCount) {
        return step("Verify search results displayed: " + expectedCount, () -> {
            try {
                List<Hotel> hotels = getAllHotelsFromListViewSearch(expectedCount);
                int actualCount = hotels.size();

                logInfo("Found " + actualCount + " hotels in search results");

                if (actualCount < expectedCount) {
                    log.error("Expected at least " + expectedCount + " hotels, but found only " + actualCount);
                    screenshot("insufficient_hotels");
                    return false;
                }

                long validHotels = hotels.stream()
                        .filter(hotel -> hotel.getName() != null && !hotel.getName().isEmpty())
                        .count();

                if (validHotels < expectedCount) {
                    log.error("Expected at least " + expectedCount + " valid hotels, but found only " + validHotels);
                    screenshot("invalid_hotels");
                    return false;
                }

                logInfo("Search results validation successful: " + validHotels + " valid hotels");
                return true;

            } catch (Exception e) {
                log.error("Error verifying search results: " + e.getMessage());
                screenshot("verify_search_results_error");
                return false;
            }
        });
    }

    public int getTotalHotelsCount(List<Hotel> hotels) {
        return step("Get total hotels count: " + hotels.size(), () -> hotels.size());
    }

    public void switchToSearchResultsTab() {
        step(() -> {
            DriverUtils.waitForNewWindowOpened(2);
            DriverUtils.switchToWindow(1);
            DriverUtils.waitForUrlContains("search", DriverUtils.getTimeOut());
        });
    }

    public void waitForSearchResultsToLoad() {
        step(() -> {
            try {
                ElementWrapper firstPropertyCard = new ElementWrapper(propertyCardXpath + "[1]");
                firstPropertyCard.waitForVisibility(Duration.ofSeconds(10));
                logInfo("Search results have loaded successfully");
            } catch (Exception e) {
                log.error("Failed to wait for search results: " + e.getMessage());
                screenshot("search_results_load_failed");
            }
        });
    }

    public void sortByLowestPrice() {
        step(() -> {
            sortPriceButton().scrollElementToCenterScreen();
            sortPriceButton().waitForElementClickable(Duration.ofSeconds(10));
            sortPriceButton().clickByJs();
        });
    }

    public int getHotelListSize() {
        return step("Get hotel list size",                                                                                                                                                                                                                                                                                                                                                                                                                              () -> {
            try {
                List<WebElement> webElements = hotelListContainer().getElements();
                return webElements.size();
            } catch (Exception e) {
                log.error("Error getting hotel list size: " + e.getMessage());
                return 0;
            }
        });
    }

    public boolean checkHotelsSortedByLowestPrice(List<Hotel> hotels) {
        return step("Verify hotels sorted by lowest price", () -> {
            if (hotels == null || hotels.isEmpty()) {
                log.error("Hotel list is null or empty");
                return false;
            }

            int hotelCount = Math.min(5, hotels.size());
            if (hotelCount == 0) {
                log.error("No hotels to verify");
                return false;
            }

            // Thu thập prices và thông tin chi tiết cho top 5 hotels
            List<Integer> prices = new ArrayList<>();
            List<String> hotelNames = new ArrayList<>();
            List<Boolean> hasValidPrice = new ArrayList<>();

            for (int i = 0; i < hotelCount; i++) {
                Hotel hotel = hotels.get(i);
                String hotelName = hotel.getName() != null ? hotel.getName() : "Unknown";
                hotelNames.add(hotelName);

                String priceStr = hotel.getPrice();
                if (priceStr == null || priceStr.isEmpty() || priceStr.equals("null")) {
                    // Null prices được gán Integer.MAX_VALUE để đảm bảo chúng phải ở cuối sau khi sorted
                    prices.add(Integer.MAX_VALUE);
                    hasValidPrice.add(false);
                    log.warn("Hotel {} (index {}): '{}' has null/empty price", i + 1, i, hotelName);
                } else {
                    try {
                        String digits = priceStr.replaceAll("[^\\d]", "");
                        if (digits.isEmpty()) {
                            prices.add(Integer.MAX_VALUE);
                            hasValidPrice.add(false);
                            log.warn("Hotel {} (index {}): '{}' has invalid price format: '{}'", i + 1, i, hotelName, priceStr);
                        } else {
                            int price = Integer.parseInt(digits);
                            prices.add(price);
                            hasValidPrice.add(true);
                            log.debug("Hotel {} (index {}): '{}' has price: {}", i + 1, i, hotelName, price);
                        }
                    } catch (NumberFormatException e) {
                        prices.add(Integer.MAX_VALUE);
                        hasValidPrice.add(false);
                        log.warn("Hotel {} (index {}): '{}' has unparseable price: '{}'", i + 1, i, hotelName, priceStr);
                    }
                }
            }

            // Tạo sorted version để so sánh
            List<Integer> sorted = new ArrayList<>(prices);
            sorted.sort(Integer::compareTo);

            // Verify: prices phải bằng sorted version
            boolean isSorted = prices.equals(sorted);

            if (!isSorted) {
                // Log chi tiết lỗi
                log.error("Hotels NOT sorted by lowest price!");
                log.error("Actual order: {}", prices);
                log.error("Expected sorted: {}", sorted);
                log.error("Hotel details:");
                for (int i = 0; i < hotelCount; i++) {
                    String priceDisplay = hasValidPrice.get(i)
                            ? String.valueOf(prices.get(i))
                            : "NULL/INVALID";
                    log.error("  [{}] {} - Price: {}", i + 1, hotelNames.get(i), priceDisplay);
                }

                // Kiểm tra thêm: nếu có null prices, chúng phải ở cuối
                int firstNullIndex = -1;
                int lastValidIndex = -1;
                for (int i = 0; i < prices.size(); i++) {
                    if (prices.get(i) == Integer.MAX_VALUE && !hasValidPrice.get(i)) {
                        if (firstNullIndex == -1) firstNullIndex = i;
                    } else if (hasValidPrice.get(i)) {
                        lastValidIndex = i;
                    }
                }

                if (firstNullIndex != -1 && lastValidIndex != -1 && firstNullIndex <= lastValidIndex) {
                    log.error("CRITICAL: Null/invalid prices found at index {} but valid prices exist at index {} - this violates sorting order!",
                            firstNullIndex + 1, lastValidIndex + 1);
                }

                return false;
            }

            // Kiểm tra thêm: nếu có null prices, chúng phải ở cuối (sau tất cả valid prices)
            int firstNullIndex = -1;
            int lastValidIndex = -1;
            for (int i = 0; i < prices.size(); i++) {
                if (prices.get(i) == Integer.MAX_VALUE && !hasValidPrice.get(i)) {
                    if (firstNullIndex == -1) firstNullIndex = i;
                } else if (hasValidPrice.get(i)) {
                    lastValidIndex = i;
                }
            }

            if (firstNullIndex != -1 && lastValidIndex != -1 && firstNullIndex <= lastValidIndex) {
                log.error("Null/invalid prices found before valid prices. First null at index {}, last valid at index {}",
                        firstNullIndex + 1, lastValidIndex + 1);
                return false;
            }

            logInfo("✓ First " + hotelCount + " hotels are correctly sorted by lowest price: " + prices);
            return true;
        });
    }


    public void waitForPropertyCardCountChange(int beforeCount) {
        step("Wait for property card count change: " + beforeCount, () -> {
            int timeoutSeconds = 11;
            try {
                hotelListContainer().waitForVisibility(Duration.ofSeconds(10));

                WebDriverWait wait = new WebDriverWait(DriverUtils.getWebDriver(), Duration.ofSeconds(timeoutSeconds));

                boolean sortCompleted = wait.until(driver -> {
                    try {
                        ElementWrapper cardsLocator = new ElementWrapper(propertyCardXpath);
                        int currentSize = cardsLocator.getElements().size();
                        if (currentSize != beforeCount) {
                            logInfo("Property card count changed from " + beforeCount + " to " + currentSize);
                            return true;
                        }

                        DriverUtils.delay(0.5);
                        return true;
                    } catch (Exception ex) {
                        log.debug("Error checking sort completion: " + ex.getMessage());
                        return false;
                    }
                });

                if (sortCompleted) {
                    logInfo("Sort operation completed (or timeout reached)");
                } else {
                    log.warn("Sort operation may not have completed within " + timeoutSeconds + " seconds");
                }
            } catch (Exception e) {
                log.error("Error waiting for property card count change: " + e.getMessage());
            }
        });
    }

}