package testCase;

import config.TestBase;
import org.example.models.Account;
import org.example.enums.Query;
import org.example.enums.TestStatus;
import org.example.pages.TestRailPage;
import org.example.utils.DriverUtils;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class TC03 extends TestBase {

    String username = "vunguyen.170803@gmail.com";
    String password = "Vunguyen_2901";
    String runUrl = "https://dutudn.testrail.io/index.php?/runs/view/1";
    Account account = new Account(username, password);
    TestRailPage testRailPage = new TestRailPage();

    @Test(description = "TC03: Login to TestRail and set filtered run results to 'Retest'")
    public void tc03_setFilteredResultsToRetest() {

        testRailPage.navigateToHomePage();
        testRailPage.login(account);
        testRailPage.openRun(runUrl);

        Map<Query, String> filterOptions = new HashMap<>();
        filterOptions.put(Query.STATUS, "Any");
        filterOptions.put(Query.TESTED_BY, "Nguyễn Minh Vũ");
        testRailPage.filterResultsBy(filterOptions, Query.STATUS, Query.TESTED_BY);

        String comment = """
                iPhone 16 (iOS 26.2)
                - Test executed on iPhone 16 with iOS 26.2
                - All functionalities work as expected
                - No issues or bugs found during testing
                - Performance is smooth and responsive
                - UI elements are displayed correctly on the screen
                - Verified compatibility with latest iOS features
                - Overall, the application is stable and ready for release
                """.stripIndent();

        testRailPage.editResults(TestStatus.BLOCKED, comment);




    }
}
