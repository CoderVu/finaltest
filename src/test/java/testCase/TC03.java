package testCase;

import config.TestBase;
import org.example.pages.TestRailPage;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import org.testng.SkipException;

/**
 * TC03 - Set filtered TestRail results to "Retest"
 */
@Slf4j
public class TC03 extends TestBase {

    @Test(description = "TC03: Login to TestRail and set filtered run results to 'Retest'")
    public void tc03_setFilteredResultsToRetest() {
        // 1) Get credentials from -Dtestrail.user / -Dtestrail.pass or env TESTRAIL_USER / TESTRAIL_PASS
        String username = "";
        String password = "";

        if (username == null || password == null) {
            String msg = "TestRail credentials not provided. Please set -Dtestrail.user / -Dtestrail.pass or env TESTRAIL_USER / TESTRAIL_PASS";
            log.error(msg);
            throw new SkipException(msg);
        }

        // 2) Prepare TestRail page and perform actions
        TestRailPage testRailPage = new TestRailPage();
        try {
            testRailPage.login(username, password);

            // use explicit run URL; adjust if you want to parametrize
            final String runUrl = "https://sonos.testrail.com/index.php?/runs/view/144115";
            testRailPage.openRun(runUrl);

            // 3) Process filtered rows and set to Retest
            testRailPage.processFilteredResultsToRetest();

            log.info("TC03 completed: processed filtered results to 'Retest'");
        } catch (Exception e) {
            log.error("TC03 failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
