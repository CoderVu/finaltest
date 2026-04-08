package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.element.SelElement;
import org.example.models.Account;
import org.example.enums.Query;
import org.example.enums.TestStatus;
import org.example.utils.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.example.core.element.SelElement.$;
import static org.example.utils.DriverUtils.getCurrentUrl;

@Slf4j
public class TestRailPage extends BasePage {

    // Page elements (wrappers)
    protected final SelElement usernameInput = $(By.id("name"));
    protected final SelElement passwordInput = $(By.id("password"));
    protected final SelElement loginButton = $(By.id("button_primary"));
    protected final SelElement filterDropdownButton = $(By.id("filterByChange"));
    protected final SelElement filterTestsContent = $(By.id("filterTestsContent"));
    protected final SelElement applyButton = $(By.id("filterTestsApply"));
    protected final SelElement clearButton = $(By.id("filterByReset"));
    protected final SelElement editLink = $(By.xpath("//span[@class='text-secondary editChange']/a"));
    protected final SelElement statusSelectChosen = $(By.id("addResultStatus_chosen"));
    protected final SelElement statusSelect = $(By.id("addResultStatus"));
    protected final SelElement commentTextarea = $(By.id("addResultComment_display")); // Hidden textarea for Froala editor
    protected final SelElement submitButton = $(By.id("addResultSubmit"));
    protected final SelElement chosenSingle = $(By.xpath("//div[@id='addResultStatus_chosen']//a[@class='chosen-single']"));
    protected final SelElement tabsElement = $(By.xpath("//div[@class='tabs']"));
    protected final SelElement qpaneClose = $(By.xpath("//div[@data-testid='qpaneCloseButton']"));
    protected final SelElement blockedUI = $(By.cssSelector(".ui-widget-overlay, .blockUI"));

    protected final String runRowXpath = "//td[@data-testid='runTestAction']";
    protected final String expandInRowXpath = "//a[.//div[@data-testid='runTestIconExpandRow']]";
    protected final String expandFallbackXpath = "//a[contains(@onclick,'App.QPane.toggleRow')]";

    /**
     * Get expand button for a specific row index
     */
    private SelElement getExpandButton(int rowIdx) {
        SelElement expandBtn = $("(%s)[%d]%s", runRowXpath, rowIdx, expandInRowXpath);
        if (!expandBtn.isExist(Duration.ofSeconds(1))) {
            expandBtn = $("(%s)[%d]%s", runRowXpath, rowIdx, expandFallbackXpath);
        }
        return expandBtn;
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

    public void login(Account account) {
        step("Login to TestRail" + account.getUsername() + ", " + account.getPassword(), () -> {
            usernameInput.waitForVisibility(Duration.ofSeconds(10));
            usernameInput.clear();
            usernameInput.setText(account.getUsername());
            passwordInput.setText(account.getPassword());
            loginButton.click();
            blockedUI.waitForInvisibility();

        });
    }

    public void openRun(String runUrl) {
        step("Open run: " + runUrl, () -> {
            DriverUtils.navigateTo(runUrl);
        });
    }

    public void filterResultsBy(Map<Query, String> queryOptions, Query... queries) {
        if (queries == null || queries.length == 0) {
            log.warn("No queries provided to filterResultsBy");
            return;
        }

        step("Filter results by: " + stream(queries)
                .map(Query::getDisplayName)
                .collect(joining(", ")), () -> {

            // clear existing filter
           if (clearButton.isExist()) {
            // clearButton.click();
            log.info("Clear button exists");
           } else {
            log.info("Clear button does not exist");
           }


            // Click the filter dropdown button to open dropdown (only once)
            filterDropdownButton.click();

            for (Query query : queries) {
                // Wait for overlays to clear
                blockedUI.waitForInvisibility();

                // Find filter group by displayName (text in the link) - more reliable than rel attribute
                SelElement filterGroup = $("//div[@id='filterTestsContent']//a[@class='link-noline' and contains(text(), '%s')]/ancestor::div[@class='filter-group filter']", query.getDisplayName());

                // Click filter name link to expand
                SelElement filterNameLink = $("//div[@id='filterTestsContent']//a[@class='link-noline' and contains(text(), '%s')]/ancestor::div[@class='filter-group filter']//a[@class='link-noline']", query.getDisplayName());
                filterNameLink.click();

                // Get option to select
                String option = queryOptions.get(query);

                filterGroup.selectOptionByText(option);
            }

            applyButton.click();

            // Wait for filter to complete - wait for loading overlays to disappear
            blockedUI.waitForInvisibility();

            // Wait for filter dropdown to close (indicates filter is processing)
            filterTestsContent.waitForDisappear();

            // Wait for rows to be ready and stable
            SelElement rowsElement = $(By.xpath(runRowXpath));
            rowsElement.waitForDisplay(Duration.ofSeconds(30));

            // Additional wait to ensure filter results are fully loaded and stable
            DriverUtils.delay(2);

            log.info("Filter completed, ready to process results");
        });
    }

    public void editResults(TestStatus status, String comment) {
        step("Process filtered results -> " + status.getDisplayName() + " - " + comment, () -> {
            // Wait for filter to complete and rows to be stable before starting
            blockedUI.waitForInvisibility();

            // Wait for rows to appear and count total rows to process
            SelElement rowsElement = $(By.xpath(runRowXpath));
            rowsElement.waitForDisplay(Duration.ofSeconds(30));

            List<WebElement> rows = rowsElement.getElements();
            int totalRows = rows == null ? 0 : rows.size();
            log.info("Found {} run row(s) for processing", totalRows);

            if (totalRows == 0) {
                logInfo("No rows to process");
                return;
            }

            // Process all rows found initially
            for (int idx = 1; idx <= totalRows; idx++) {
                // Get expand button for this row
                SelElement expandBtn = getExpandButton(idx);

                // Expand row if not already expanded
                boolean isAlreadyExpanded = qpaneClose.isExist(Duration.ofSeconds(2));
                if (!isAlreadyExpanded && expandBtn.isExist(Duration.ofSeconds(2))) {
                    expandBtn.scrollToView(0, -100); // Scroll with offset to avoid header overlap
                    expandBtn.clickByJs(); // Use JS click to avoid interception
                }

                // Click edit link
                editLink.scrollToView();
                editLink.click();

                // Wait for QPane to open and form to load
                tabsElement.scrollToView();
                statusSelectChosen.waitForVisibility(Duration.ofSeconds(15));

                // Select status
                chosenSingle.click();
                SelElement chosenOption = $("//div[@id='addResultStatus_chosen']//ul[@class='chosen-results']//li[contains(text(), '%s')]", status.getDisplayName());
                chosenOption.click();

                // Set comment
                commentTextarea.waitForVisibility(Duration.ofSeconds(5));
                commentTextarea.setTextInEditor(comment);
                log.debug("Set comment using Froala editor");

                // Submit
                submitButton.click();
                logInfo("Submitted " + status.getDisplayName() + " for row " + idx);

                // Close QPane if exists
                if (qpaneClose.isExist(Duration.ofSeconds(2))) {
                    qpaneClose.click();
                }

                // Collapse row if expand button exists
                if (expandBtn.isExist(Duration.ofSeconds(2))) {
                    expandBtn.click();
                }

                // Wait before processing next row
                DriverUtils.delay(1);
            }
        });
    }
}
