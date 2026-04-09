package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.assertion.AwaitAssert;
import org.example.utils.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static org.example.configure.Config.getMaxActionRetries;
import static org.example.utils.DriverUtils.getWebDriver;

@Slf4j
public class SelElement implements ISelElement {

    protected final By byLocator;

    public SelElement(By byLocator) {
        this.byLocator = byLocator;
    }

    public static SelElement $(By byLocator) {
        return new SelElement(byLocator);
    }

    public static SelElement $(String xpathLocator, Object... args) {
        return new SelElement(xpathLocator, args);
    }

    public SelElement(String xpathLocator, Object... args) {
        this(By.xpath(formatLocator(xpathLocator, args)));
    }

    private static String formatLocator(String locator, Object... args) {
        if (args == null || args.length == 0) {
            return locator;
        }
        return String.format(locator, args);
    }

    private <T> T doWithRetry(Supplier<T> action) {
        int maxAttempts = Math.max(1, getMaxActionRetries());
        int attempt = 1;
        Exception lastError = null;

        do {
            try {
                return action.get();
            } catch (
                    StaleElementReferenceException |
                    NoSuchElementException e
            ) {
                lastError = e;

                log.warn("Retry {}/{} for '{}' due to {}",
                        attempt,
                        maxAttempts,
                        getLocator(),
                        e.getClass().getSimpleName());

                DriverUtils.delay(0.5);
                attempt++;
            }
        } while (attempt <= maxAttempts);

        throw new RuntimeException(
                "Failed after " + maxAttempts + " attempts for " + getLocator(),
                lastError
        );
    }

    private void doWithRetry(Runnable action) {
        doWithRetry(() -> {
            action.run();
            return null;
        });
    }

    @Override
    public By getLocator() {
        return this.byLocator;
    }

    @Override
    public WebElement getElement() {
        log.debug("Get element for locator: {}", byLocator.toString());
        return doWithRetry(() -> getWebDriver().findElement(byLocator));
    }

    @Override
    public List<WebElement> getElements() {
        log.debug("Get elements for locator: {}", byLocator.toString());
        return doWithRetry(() -> getWebDriver().findElements(byLocator));
    }

    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }

    public AwaitAssert.ElementExpectation expect() {
        return AwaitAssert.expect(this);
    }

    // Backward-compatible aliases
    public void shouldBeVisible() { expect().toBeVisible(); }
    public void shouldNotBeVisible() { expect().toBeHidden(); }
    public void shouldBeEnabled() { expect().toBeEnabled(); }
    public void shouldBeDisabled() { expect().toBeDisabled(); }
    public void shouldHaveText(String expected) { expect().toHaveText(expected); }
    public void shouldContainText(String substring) { expect().toContainText(substring); }
    public void shouldHaveAttribute(String attribute, String expected) { expect().toHaveAttribute(attribute, expected); }
    public void shouldHaveValue(String expected) { expect().toHaveValue(expected); }

    // ========== ACTIONS ==========

    @Override
    public void click() {
        doWithRetry(() -> {
            log.debug("Click on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.click();
        });
    }

    @Override
    public void click(int x, int y) {
        doWithRetry(() -> {
            log.debug("Click at offset ({}, {}) on {}", x, y, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).click().build().perform();
        });
    }

    @Override
    public void clickByJs() {
        doWithRetry(() -> {
            log.debug("Click by JS on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver()).executeScript("arguments[0].click();", element);
        });
    }

    @Override
    public void doubleClick() {
        doWithRetry(() -> {
            log.debug("Double click on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).doubleClick(element).build().perform();
        });
    }

    @Override
    public void setText(String text) {
        doWithRetry(() -> {
            log.debug("Set text '{}' on {}", text, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(text);
        });
    }

    /**
     * Set text in Froala editor (contenteditable div).
     * Finds the .fr-element.fr-view element inside the current element and sets text using Actions.
     *
     * @param text The text to set in the editor
     */
    public void setTextInEditor(String text) {
        doWithRetry(() -> {
            log.debug("Set text '{}' in Froala editor on {}", text, getLocator().toString());
            WebElement editorContainer = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            
            // Find the contenteditable div inside Froala editor
            WebElement editableDiv = editorContainer.findElement(By.cssSelector(".fr-element.fr-view"));
            WaitUtils.waitFor(ExpectedConditions.visibilityOf(editableDiv), Duration.ofSeconds(5));
            
            // Use Actions to interact with contenteditable div
            Actions actions = new Actions(getWebDriver());
            
            // Click to focus on the editor
            actions.click(editableDiv).perform();
            
            // Clear existing content: select all and delete
            actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
            actions.sendKeys(Keys.DELETE).perform();
            
            // Set new text
            actions.sendKeys(editableDiv, text).perform();
        });
    }

    @Override
    public void clear() {
        doWithRetry(() -> {
            log.debug("Clear text on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.clear();
        });
    }

    @Override
    public void submit() {
        doWithRetry(() -> {
            log.debug("Submit on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.submit();
        });
    }

    @Override
    public void focus() {
        doWithRetry(() -> {
            log.debug("Focus on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            DriverUtils.execJavaScript("arguments[0].focus();", element);
        });
    }

    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        doWithRetry(() -> {
            log.debug("Drag and drop by offset ({}, {}) on {}", xOffset, yOffset, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDropBy(element, xOffset, yOffset).build().perform();
        });
    }

    @Override
    public void dragAndDrop(ISelElement target) {
        doWithRetry(() -> {
            log.debug("Drag element {} to target {}", getLocator().toString(), target.getLocator().toString());
            WebElement sourceElement = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            WebElement targetElement = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(target.getLocator()));
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDrop(sourceElement, targetElement).build().perform();
        });
    }

    @Override
    public void moveTo() {
        doWithRetry(() -> {
            log.debug("Move to {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void moveTo(int x, int y) {
        doWithRetry(() -> {
            log.debug("Move to offset ({}, {}) on {}", x, y, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).build().perform();
        });
    }

    @Override
    public void moveToCenter() {
        doWithRetry(() -> {
            log.debug("Move to center of {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void mouseHoverJScript() {
        doWithRetry(() -> {
            log.debug("Mouse hover by JS on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            ((JavascriptExecutor) getWebDriver()).executeScript(mouseOverScript, element);
        });
    }

    @Override
    public void setAttributeJS(String attributeName, String value) {
        doWithRetry(() -> {
            log.debug("Set attribute '{}'='{}' for {}", attributeName, value, getLocator().toString());
            log.debug("Set attribute for {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver())
                    .executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                            element);
        });
    }

    @Override
    public void checkCheckBoxByJs() {
        doWithRetry(() -> {
            log.debug("Check checkbox by JS for {}", getLocator().toString());
            log.debug("Check checkbox by JS for {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver())
                    .executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));",
                            element);
        });
    }

    // ========== SCROLLS ==========

    @Override
    public void scrollElementToCenterScreen() {
        doWithRetry(() -> {
            log.debug("Scroll element to center of screen: {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", element);
        });
    }

    @Override
    public void scrollToView() {
        doWithRetry(() -> {
            log.debug("Scroll element into view: {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView(true);", element);
        });
    }

    @Override
    public void scrollToView(int offsetX, int offsetY) {
        doWithRetry(() -> {
            log.debug("Scroll element into view with extra offset ({}, {}): {}", offsetX, offsetY, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            String script = String.format(
                    "arguments[0].scrollIntoView(true); window.scrollBy(%d, %d);",
                    offsetX, offsetY);
            js.executeScript(script, element);
        });
    }

    // ========== GETTERS ==========

    @Override
    public String getText() {
        return doWithRetry(() -> {
            log.debug("Get text of {}", getLocator().toString());
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.getText();
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getValue() {
        return doWithRetry(() -> {
            log.debug("Get value of {}", getLocator().toString());
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.getAttribute("value");
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getAttribute(String attributeName) {
        log.debug("Get attribute '{}' of {}", attributeName, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.getAttribute(attributeName);
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getClassName() {
        log.debug("Get class name of {}", getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.getAttribute("class");
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getTagName() {
        log.debug("Get tag name of {}", getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.getTagName();
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public WebElement getChildElement(String xpath) {
        log.debug("Get child element '{}' of {}", xpath, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.findElement(buildChildLocator(xpath));
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }

    @Override
    public List<WebElement> getChildElements(String xpath) {
        log.debug("Get child elements '{}' of {}", xpath, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = getWebDriver().findElement(getLocator());
                return element.findElements(buildChildLocator(xpath));
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    // ========== CHECKS ==========
    // No doWithRetry here to avoid missing bugs due to retries

    @Override
    public boolean isVisible() {
        return isVisible(DriverUtils.getTimeOut());
    }

    @Override
    public boolean isVisible(Duration timeout) {
        try {
            WebElement element = getWebDriver().findElement(getLocator());
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            log.debug("isVisible() single-shot false for locator '{}': {}", getLocator(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        try {
            log.debug("is control enabled or not: {}", getLocator().toString());
            return getElement().isEnabled();
        } catch (Exception e) {
            log.error("IsEnabled: Has error with control '{}': {}", getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }

    @Override
    public boolean isSelected() {
        try {
            log.debug("is control selected or not: {}", getLocator().toString());
            return getElement().isSelected();
        } catch (Exception e) {
            log.error("IsSelected: Has error with control '{}': {}", getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }

    @Override
    public boolean isClickable() {
        try {
            WebElement element = getWebDriver().findElement(getLocator());
            return element.isDisplayed() && element.isEnabled();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    @Override
    public boolean isExist() {
        return isExist(DriverUtils.getTimeOut());
    }

    @Override
    public boolean isExist(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()), actualTimeout);
            return true;
        } catch (TimeoutException e) {
            log.debug("isExist() - timeout for locator '{}'", getLocator());
            return false;
        }
    }

    // ========== WAITS ==========

    @Override
    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForVisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not visible after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementVisible(Duration timeout) {
        waitForVisibility(timeout);
    }

    @Override
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementClickable(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not clickable after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForDisplay(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "Element not displayed after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.error("waitForDisplay timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForInvisibility() {
        waitForInvisibility(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForInvisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForInvisibility timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.",
                    actualTimeout.getSeconds(), getLocator().toString());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForDisappear() {
        waitForDisappear(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForDisappear(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "Element still visible after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.warn("Element '{}' still visible after {} seconds", getLocator().toString(), actualTimeout.getSeconds());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementEnabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(driver -> {
                try {
                    return driver.findElement(getLocator()).isEnabled();
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not enabled after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementEnabled timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementDisabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(driver -> {
                try {
                    return !driver.findElement(getLocator()).isEnabled();
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not disabled after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementDisabled timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForTextToBePresent(String text) {
        waitForTextToBePresent(text, DriverUtils.getTimeOut());
    }

    @Override
    public void waitForTextToBePresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.textToBePresentInElementLocated(getLocator(), text), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForTextToBePresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBePresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForTextToBeNotPresent(String text) {
        waitForTextToBeNotPresent(text, DriverUtils.getTimeOut());
    }

    @Override
    public void waitForTextToBeNotPresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(getLocator(), text)), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForTextToBeNotPresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBeNotPresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForValuePresentInAttribute(String attribute, String value) {
        waitForValuePresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }

    @Override
    public void waitForValuePresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.attributeContains(getLocator(), attribute, value), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForValuePresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValuePresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waitForValueNotPresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }

    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(
                    ExpectedConditions.not(ExpectedConditions.attributeContains(getLocator(), attribute, value)),
                    actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForValueNotPresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValueNotPresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForStalenessOfElement(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WebElement element = getElement();
            WaitUtils.waitFor(ExpectedConditions.stalenessOf(element), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForStalenessOfElement timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForStalenessOfElement: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            log.error("waitForStalenessOfElement: Has error with control '{}': {}",
                    getLocator().toString(), e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw new RuntimeException("waitForStalenessOfElement error for control: " + getLocator().toString(), e);
        }
    }

    // ========== SELECT ==========

    @Override
    public Select getSelect() {
        return new Select(getElement());
    }


    /**
     * Find select element - supports multiple dropdown patterns:
     * 1. Direct select element
     * 2. Select inside div/span/other containers
     * 3. Select with rel='items' attribute (common in modern web apps)
     * 4. Any select element as fallback
     */
    private WebElement findSelectElement(WebElement root) {
        // 1) If root is select → use it directly
        if ("select".equalsIgnoreCase(root.getTagName())) {
            return root;
        }
        
        // 2) Try to find select with common patterns (priority order)
        List<By> selectLocators = List.of(
            By.xpath(".//select[@rel='items']"),           // Common in modern apps (TestRail, etc.)
            By.xpath(".//select[contains(@class, 'select')]"), // Bootstrap, Material-UI patterns
            By.xpath(".//select[@id]"),                    // Select with ID
            By.xpath(".//select[@name]"),                 // Select with name attribute
            By.xpath(".//select")                         // Any select element (fallback)
        );
        
        for (By locator : selectLocators) {
            List<WebElement> selects = root.findElements(locator);
            if (!selects.isEmpty()) {
                WebElement select = selects.get(0);
                if (select.isDisplayed() && select.isEnabled()) {
                    log.debug("Found select element using locator: {}", locator);
                    return select;
                }
            }
        }
        
        throw new NoSuchElementException("No <select> element found inside element: " + getLocator());
    }

    public void selectOptionByText(String text) {
        doWithRetry(() -> {
            log.debug("Select option by text '{}' from {}", text, getLocator());
            
            // Wait for root element to be visible
            WebElement root = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            
            // Find select element (supports multiple patterns)
            WebElement selectElement = findSelectElement(root);

            // Wait for select to be clickable
            WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(selectElement), Duration.ofSeconds(5));
            
            // Perform selection
            Select select = new Select(selectElement);
            log.debug("Selecting option by text '{}' from select element", text);
            select.selectByVisibleText(text);
        });
    }

    public void selectOptionByValue(String value) {
        doWithRetry(() -> {
            log.debug("Select option by value '{}' from {}", value, getLocator());

            // Wait for root element to be visible
            WebElement root = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));

            // Find select element (supports multiple patterns)
            WebElement selectElement = findSelectElement(root);
            
            // Wait for select to be clickable
            WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(selectElement), Duration.ofSeconds(5));

            // Perform selection
            Select select = new Select(selectElement);
            log.debug("Selecting option by value '{}' from select element", value);
            select.selectByValue(value);
        });
    }



    // ========== HELPERS ==========

    private By buildChildLocator(String xpath) {
        return new ByChained(By.xpath("."), By.xpath(xpath));
    }

}

