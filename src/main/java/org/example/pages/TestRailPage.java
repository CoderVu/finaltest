package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.BaseElement;
import org.example.utils.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

@Slf4j
public class TestRailPage extends BasePage {

    // Page elements (wrappers)
    protected final BaseElement usernameInput = BaseElement.$(By.id("name"));
    protected final BaseElement passwordInput = BaseElement.$(By.id("password"));
    protected final BaseElement loginButton = BaseElement.$(By.id("button_primary"));

    protected final String runRowXpath = "//td[@data-testid='runTestAction']";
    // Prefer the anchor that contains the expand icon element (data-testid='runTestIconExpandRow'),
    // fallback to the anchor that uses App.QPane.toggleRow(...) in onclick.
    protected final String expandInRowXpath = ".//a[.//div[@data-testid='runTestIconExpandRow']]";
    protected final String expandFallbackXpath = ".//a[contains(@onclick,'App.QPane.toggleRow')]";
    protected final String editLinkInRowXpath = ".//span[@class='text-secondary editChange']/a";
    protected final BaseElement globalEditLink = BaseElement.$(By.xpath("//span[@class='text-secondary editChange']/a"));
    protected final BaseElement statusSelect = BaseElement.$(By.id("addResultStatus"));
    protected final BaseElement submitButton = BaseElement.$(By.id("addResultSubmit"));

    // Default comment to set when updating a result
    protected final String defaultComment =
            "iPhone 16 (iOS 26.2)\n" +
            "CR: 80.35.6 - 2025 App 6.1.1 - release (ios)\n" +
            "Players:\n" +
            "\n" +
            "- 86.2-70230-v17.2.2-2025_Player_CEP20_3-RC-2\n" +
            "- 92.0-71170-v17.7-2025_Player_6.1-RC-1\n" +
            "- 92.0-72090-v17.7-2025_Player_6.1-RC-2-SP_1 (Optimo 1, Optimo 2, Graceland, Prima, Raven, Lasso)\n" +
            "- Duke: 3.9.9 - 2025 Ace 3 SP 1 nữa";

    // close button for the QPane that appears after edit
    protected final String qpaneCloseXpath = "//div[@data-testid='qpaneCloseButton']";
    protected final BaseElement qpaneClose = BaseElement.$(By.xpath("//div[@data-testid='qpaneCloseButton']"));

    public void login(String username, String password) {
        step("Login to TestRail", () -> {
            DriverUtils.navigateTo("https://sonos.testrail.com");
            usernameInput.waitForVisibility(Duration.ofSeconds(10));
            usernameInput.clear();
            usernameInput.setText(username);
            passwordInput.setText(password);
            loginButton.click();
            // wait briefly for navigation/auth completion
            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-widget-overlay, .blockUI")), Duration.ofSeconds(10));
            logInfo("Logged in as " + username);
        });
    }

    public void openRun(String runUrl) {
        step("Open run: " + runUrl, () -> {
            DriverUtils.navigateTo(runUrl);
            // give page a chance to render
            WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(By.xpath(runRowXpath)), Duration.ofSeconds(10));
            logInfo("Opened run page");
        });
    }

    public void processFilteredResultsToRetest() {
        step("Process filtered results -> Retest", () -> {
            while (true) {
                // Wait for rows to appear (some pages render rows asynchronously).
                try {
                    WaitUtils.waitFor(driver -> {
                        List<WebElement> r = DriverUtils.getWebDriver().findElements(By.xpath(runRowXpath));
                        return r != null && !r.isEmpty();
                    }, Duration.ofSeconds(30));
                } catch (Exception waitEx) {
                    // timed out waiting for rows — proceed to fetch whatever is present and log.
                    log.warn("Timed out waiting for run rows to appear: {}", waitEx.getMessage());
                }

                // refetch rows each pass using BaseElement so we can use getChildElement
                List<WebElement> rows = BaseElement.$(By.xpath(runRowXpath)).getElements();
                int rowsFound = rows == null ? 0 : rows.size();
                log.info("Found {} run row(s) for processing", rowsFound);
                if (rowsFound == 0) {
                    logInfo("No rows to process");
                    break;
                }

                boolean progressed = false;
                int total = rows.size();
                for (int idx = 1; idx <= total; idx++) {
                    try {
                        // wait for overlays to clear
                        WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-widget-overlay, .blockUI")), Duration.ofSeconds(5));

                        // create a BaseElement that represents this specific row: (runRowXpath)[idx]
                        BaseElement rowEl = BaseElement.$("(%s)[%d]", runRowXpath, idx);

                        WebElement expandBtn = null;
                        try {
                            expandBtn = rowEl.getChildElement(expandInRowXpath);
                        } catch (Exception e) {
                            try {
                                expandBtn = rowEl.getChildElement(expandFallbackXpath);
                            } catch (Exception ex) {
                                log.warn("No expand button found for row {}: {}", idx, ex.getMessage());
                                expandBtn = null;
                            }
                        }
                        if (expandBtn == null) {
                            log.warn("No expand button found for a row, skipping");
                            continue;
                        }

                        // open row (safe click with JS fallback)
                        safeClick(expandBtn);

                        // find edit link scoped to this row (using indexed XPath to avoid BaseElement retries).
                        // If not found, fallback to global edit link.
                        WebElement editLink = null;
                        try {
                            String rowScopedEditXpath = String.format("(%s)[%d]//span[@class='text-secondary editChange']/a", runRowXpath, idx);
                            List<WebElement> found = DriverUtils.getWebDriver().findElements(By.xpath(rowScopedEditXpath));
                            if (found != null && !found.isEmpty()) {
                                editLink = found.get(0);
                            } else {
                                // fallback to global edit link presence/clickable
                                try {
                                    editLink = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='text-secondary editChange']/a")));
                                } catch (Exception ex) {
                                    editLink = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='text-secondary editChange']/a")));
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error locating edit link for row {}: {}", idx, e.getMessage());
                            editLink = null;
                        }

                        if (editLink == null) {
                            log.warn("Edit link not found for row {}", idx);
                            collapseIfPossible(expandBtn);
                            continue;
                        }

                        safeClick(editLink);

                        // wait for status select
                        WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(By.id("addResultStatus")), Duration.ofSeconds(10));

                        // set to Retest (value "4")
                        try {
                            WebElement sel = DriverUtils.getWebDriver().findElement(By.id("addResultStatus"));
                            try {
                                new Select(sel).selectByValue("4");
                            } catch (Exception ex) {
                                DriverUtils.execJavaScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'));", sel, "4");
                            }
                        } catch (Exception e) {
                            log.warn("Status select not available: {}", e.getMessage());
                        }

                        // set comment (try normal way then JS fallback)
                        try {
                            BaseElement commentEl = BaseElement.$(By.id("addResultComment"));
                            commentEl.waitForVisibility(Duration.ofSeconds(5));
                            commentEl.clear();
                            commentEl.setText(defaultComment);
                        } catch (Exception e) {
                            try {
                                WebElement commentWeb = DriverUtils.getWebDriver().findElement(By.id("addResultComment"));
                                DriverUtils.execJavaScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));", commentWeb, defaultComment);
                            } catch (Exception ex) {
                                log.warn("Could not set comment field, proceeding without comment: {}", ex.getMessage());
                            }
                        }

                        // submit
                        try {
                            WebElement sub = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.id("addResultSubmit")), Duration.ofSeconds(10));
                            sub.click();
                            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.id("addResultStatus")), Duration.ofSeconds(10));
                            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.className("blockUI")), Duration.ofSeconds(10));
                            logInfo("Submitted Retest for a row");
                        } catch (Exception e) {
                            log.warn("Submit failed: {}", e.getMessage());
                            collapseIfPossible(expandBtn);
                            continue;
                        }
                        // Close the QPane edit panel (click the close icon) so we can safely move to another row
                        try {
                            WebElement closeBtn = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(By.xpath(qpaneCloseXpath)), Duration.ofSeconds(5));
                            try {
                                DriverUtils.execJavaScript("arguments[0].scrollIntoView({block:'center'});", closeBtn);
                            } catch (Exception ignored) {}
                            try {
                                closeBtn.click();
                            } catch (Exception clickEx) {
                                DriverUtils.execJavaScript("arguments[0].click();", closeBtn);
                            }
                            // wait for the close button/pane to disappear
                            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(By.xpath(qpaneCloseXpath)), Duration.ofSeconds(5));
                        } catch (Exception e) {
                            log.warn("Could not click qpane close button (proceeding): {}", e.getMessage());
                        }

                        // collapse the row to avoid re-processing
                        collapseIfPossible(expandBtn);

                        progressed = true;
                        // after processing one, break to refetch rows
                        break;
                    } catch (Exception e) {
                        log.warn("Error processing a row: {}", e.getMessage());
                        // continue with next row
                    }
                }

                if (!progressed) {
                    logInfo("No progress in this pass, exiting");
                    break;
                }
            }
            logInfo("Finished processing filtered results");
        });
    }

    // ---------- Helpers ----------

    private void safeClick(WebElement el) {
        try {
            DriverUtils.execJavaScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
        try {
            el.click();
        } catch (Exception e) {
            try {
                DriverUtils.execJavaScript("arguments[0].click();", el);
            } catch (Exception ex) {
                log.warn("safeClick failed: {}", ex.getMessage());
                throw new RuntimeException("Cannot click element", ex);
            }
        }
    }

    private void collapseIfPossible(WebElement expandBtn) {
        try {
            safeClick(expandBtn);
        } catch (Exception e) {
            // best effort only
        }
    }

}
